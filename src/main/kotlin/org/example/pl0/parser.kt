package org.example.pl0

/**
 * program := block
 * block := statement-list
 * constDecl := const identifier = number{, identifier = number }
 * varDecl := var identifier{, identifier}
 * funcDecl := function ident ([ident{, ident}]) { block };
 * statement := identifier = expression
 *              | if ( condition ) "{" statement-list "}"
 *              | while (condition) do "{" statement-list "}"
 *              | return expression
 *              | write expression
 *              | writeln
 *              | constDecl
 *              | varDecl
 *              | funcDecl
 * condition := expression =|<>|<|>|<=|>= expression
 * expression := [+|-] term {+ term}
 * term := factor {(*|/) factor }
 * factor := ident
 *          | number
 *          | ident ([expression{, expression}])
 *          | ( expression )
 *
 */
class Parser(lexer: Lexer) {
    private val levelIndex = mutableMapOf(-1 to 0)
    private val levelAddr = mutableMapOf(-1 to 0)
    private var level: Int = 0
    private var localAddr: Int = 2

    private val tokenizer = lexer
    private val nameTable = mutableListOf<TableEntry>()
    private val codes = mutableListOf<Instruction>()

    private var currentToken: Token = tokenizer.nextToken()

    private fun nextToken(): Token {
        val next = tokenizer.nextToken()
        currentToken = next
        return next
    }

    fun parse(): List<Instruction> {
        parseBlock()
        return codes
    }

    private fun blockBegin(firstAddr: Int = 2) {
        /* level => Table Index */
        levelIndex[level] = nameTable.size
        levelAddr[level] = localAddr
        localAddr = firstAddr
        level++
        return
    }

    /* {statement}) */
    private fun parseStatementList() {
        while (currentToken is IdentifierToken || currentToken is BeginToken ||
            currentToken is IfToken || currentToken is WhenToken || currentToken is WhileToken || currentToken is ReturnToken ||
            currentToken is WriteToken || currentToken is WritelnToken
            || currentToken is VarToken || currentToken is ConstToken || currentToken is FuncToken
        ) {
            parseStatement()
        }
    }

    /* block => statement_list  */
    private fun parseBlock(funcEntry: FuncEntry? = null) {
        parseStatementList()
        /* returnで終わっていない関数の場合はRetを生成 */
        if (codes.last() !is Ret) {
            /* TODO: stackのpush, popで関数の引数を管理したい。関数の実行時に引数の分popすべき。 */
            codes.add(Ret(level, funcEntry?.parCount ?: 0))
        }
        /* block end */
        blockEnd()
    }

    private fun blockEnd() {
        level--
        var i = checkNotNull(levelIndex[level])
        (i until nameTable.size).forEach { _ ->
            nameTable.removeAt(nameTable.size - 1)
        }
        localAddr = checkNotNull(levelAddr[level])
    }

    /* OK */
    /* const ident = number{, ident = number} */
    private fun parseConstDecl() {
        /* const ident = number{, ident = number} */
        assertAndReadToken<ConstToken>()
        while (true) {
            val id = parseIdentifier()
            assertAndReadToken<AssignToken>()
            val number = assertAndReadToken<IntToken>()
            addEntry(ConstEntry(id.literal, number.literal.toInt()))
            if (currentToken !is CommaToken) {
                break
            }
            assertAndReadToken<CommaToken>()
        }
    }

    /* OK */
    private fun parseIdentifier(): IdentifierToken {
        return assertAndReadToken()
    }

    private fun addEntry(tableEntry: TableEntry) {
        nameTable.add(tableEntry)
        codes.add(Ict(1))
        if (tableEntry is VarEntry) {
            localAddr++;
        }
    }

    /* OK */
    private fun parseVarDecl() {
        /*  var ident{, ident} */
        assertAndReadToken<VarToken>()
        while (true) {
            val identifier = assertAndReadToken<IdentifierToken>()
            /* add varDecl */
            val entry = VarEntry(identifier.literal, level, localAddr)
            addEntry(entry)
            if (currentToken !is CommaToken) {
                break
            }
            assertAndReadToken<CommaToken>()
        }
    }

    /* OK */
    /* TODO: wrap jmp. should not return access to jmp */
    private fun skipStart(): Jmp {
        val jmp = Jmp()
        codes.add(jmp)
        return jmp
    }

    private fun skipEnd(jmp: Jmp) {
        jmp.value = codes.size
    }

    private fun parseFuncDecl() {
        /* function ident ([ident{, ident}]) {block} */
        assertAndReadToken<FuncToken>()
        val identifierToken = assertAndReadToken<IdentifierToken>()
        /* 呼び出し時には関数の登録と関数のスキップをスキップ codes.size + 2 */
        val funcEntry = FuncEntry(identifierToken.literal, level, codes.size + 2, 0)
        addEntry(funcEntry)
        /* 宣言時には実行をスキップ */
        val jmp = skipStart()
        /* line: codes.size + 2 */
        val index = nameTable.size
        assertAndReadToken<LParenToken>()
        blockBegin()
        /* )ジャない場合 */
        if (currentToken is IdentifierToken) {
            while (true) {
                val parToken = assertAndReadToken<IdentifierToken>()
                addEntry(ParEntry(parToken.literal, level))
                /* TODO: codes.add()*/
                funcEntry.parCount += 1
                if (currentToken !is CommaToken) {
                    break
                }
                assertAndReadToken<CommaToken>()
            }
        }
        assertAndReadToken<RParentToken>()
        var i = 0;
        while (i < funcEntry.parCount) {
            (nameTable[index + i] as ParEntry).parAddr = i - funcEntry.parCount
            i++
        }
        assertAndReadToken<LBraceToken>()
        parseBlock(funcEntry)
        /* ここまでスキップ */
        skipEnd(jmp)
        assertAndReadToken<RBraceToken>()
    }


    /* parse statement; */
    private fun parseStatement() {
        when (currentToken) {
            is IdentifierToken -> {
                /* OK */
                /* ident := expression */
                val token = assertAndReadToken<IdentifierToken>()
                when (val entry = findEntry(token.literal)) {
                    /* var decl, var assign */
                    is VarEntry -> {
                        addEntry(entry)
                        assertAndReadToken<AssignToken>()
                        parseExpression()
                        codes.add(Sto(entry.level, entry.addr))
                    }
                    /* reassign to parameter */
                    is ParEntry -> {
                        addEntry(entry)
                        assertAndReadToken<AssignToken>()
                        parseExpression()
                        codes.add(Sto(entry.level, entry.parAddr))
                    }
                    else -> {
                        error("unexpected entry $entry")
                    }
                }
            }
            /* OK */
            is IfToken -> {
                /* if ( condition ) then { statement } */
                assertAndReadToken<IfToken>()
                assertAndReadToken<LParenToken>()
                parseCondition()
                assertAndReadToken<RParentToken>()
                assertAndReadToken<LBraceToken>()
                val jpc = Jpc()
                codes.add(jpc)
                parseStatement()
                /* back patch */
                jpc.value = codes.size
                assertAndReadToken<RBraceToken>()
            }
            /* OK */
            is WhileToken -> {
                /* while ( condition ) do { statement-list }*/
                assertAndReadToken<WhileToken>()
                assertAndReadToken<LParenToken>()
                val i = codes.size
                parseCondition()
                assertAndReadToken<RParentToken>()
                val jpc = Jpc()
                codes.add(jpc)
                assertAndReadToken<DoToken>()
                assertAndReadToken<LBraceToken>()
                parseStatementList()
                codes.add(Jmp(i))
                jpc.value = codes.size
                assertAndReadToken<RBraceToken>()
            }
            /* OK */
            is ReturnToken -> {
                assertAndReadToken<ReturnToken>()
                parseExpression()
                val funcEntry = nameTable[checkNotNull(levelIndex[level - 1]) - 1]
                if (funcEntry !is FuncEntry) {
                    error("entry should be function")
                }
                codes.add(Ret(level, funcEntry.parCount))
            }
            /* Write */
            is WriteToken -> {
                /* OK */
                assertAndReadToken<WriteToken>()
                parseExpression()
                codes.add(Wrt())
            }
            is WritelnToken -> {
                assertAndReadToken<WritelnToken>()
                /* OK */
                codes.add(Wrl())
            }
            /* Decl */
            is VarToken -> {
                parseVarDecl()
            }
            is FuncToken -> {
                parseFuncDecl()
            }
            is ConstToken -> {
                parseConstDecl()
            }
            else -> {
                println("empty statement")
            }
        }
    }

    /* OK* */
    private fun parseCondition() {
        /* expression [=|<>|<|>|<=|>=] expression */
        parseExpression()
        val token = currentToken
        when (token) {
            is EqualToken -> assertAndReadToken<EqualToken>()
            is NotEqToken -> assertAndReadToken<NotEqToken>()
            is LssToken -> assertAndReadToken<LssToken>()
            is LssEqToken -> assertAndReadToken<LssEqToken>()
            is GrtToken -> assertAndReadToken<GrtToken>()
            is GrtEqToken -> assertAndReadToken<GrtEqToken>()
            else -> error("should be comp op")
        }
        parseExpression()
        when (token) {
            is EqualToken -> codes.add(Eq())
            is NotEqToken -> codes.add(NotEq())
            is LssToken -> codes.add(Lss())
            is LssEqToken -> codes.add(LssEq())
            is GrtToken -> codes.add(Grt())
            is GrtEqToken -> codes.add(GrtEq())
            else -> error("should be comp op")
        }
    }

    /* OK */
    private fun parseExpression() {
        /* [+|-]? {term // [+|-] term} */
        when (currentToken) {
            is PlusToken -> {
                nextToken()
                parseTerm()
                codes.add(Add())
            }
            is MinusToken -> {
                nextToken()
                parseTerm()
                codes.add(Sub())
            }
            else -> {
                parseTerm()
            }
        }
        while (currentToken is PlusToken || currentToken is MinusToken) {
            val token = currentToken
            nextToken()
            parseTerm()
            when (token) {
                is PlusToken -> codes.add(Add())
                is MinusToken -> codes.add(Sub())
                else -> error("unexpected path")
            }
        }
    }

    /* OK */
    private fun parseTerm() {
        /* factor {*\/ factor} */
        parseFactor()
        while (currentToken is MultiToken || currentToken is DivToken) {
            val token = currentToken
            nextToken()
            parseFactor()
            when (token) {
                is MultiToken -> codes.add(Mul())
                is DivToken -> codes.add(Div())
                else -> error("unexpected path here")
            }
        }
    }

    private fun findEntry(name: String): TableEntry {
        return checkNotNull(nameTable.findLast { it.name == name })
    }

    /* OK? */
    private fun parseFactor() {
        when (currentToken) {
            is IdentifierToken -> {
                /* semantic */
                when (val entry = findEntry(parseIdentifier().literal)) {
                    /* OK: */
                    is ConstEntry -> {
                        codes.add(Lit(entry.value))
                    }
                    /* OK: */
                    is VarEntry -> {
                        codes.add(Lod(entry.level, entry.addr))
                    }
                    /* OK: */
                    is ParEntry -> {
                        codes.add(Lod(entry.level, entry.parAddr))
                    }
                    is FuncEntry -> {
                        /* f({a //, }?) */
                        var parCount = 0;
                        assertAndReadToken<LParenToken>()
                        /* {a //, } */
                        if (currentToken !is RParentToken) {
                            while (true) {
                                parseExpression()
                                parCount++
                                if (currentToken !is CommaToken) {
                                    break
                                }
                                assertAndReadToken<CommaToken>()
                            }
                        }
                        if (entry.parCount != parCount) {
                            error("count arguments not match")
                        }
                        assertAndReadToken<RParentToken>()
                        /* gen code T Call */
                        codes.add(Cal(entry.level, entry.rAddr))
                    }
                }
            }
            is IntToken -> {
                /* OK */
                val intToken = assertAndReadToken<IntToken>()
                /* semantic */
                codes.add(Lit(intToken.literal.toInt()))
            }
            is LParenToken -> {
                assertAndReadToken<LParenToken>()
                parseExpression()
                assertAndReadToken<RParentToken>()
            }
        }
    }

    private inline fun <reified T> assertAndReadToken(): T {
        val token = assertTokenIs<T>(currentToken)
        nextToken()
        return token
    }

    private inline fun <reified T> assertTokenIs(token: Token): T {
        if (token is T) {
            return token
        } else {
            error("not expected token ${token.kind} ${token.literal}")
        }
    }
}