package org.example.pl0

import org.example.*


private operator fun Regex.contains(char: Char?): Boolean {
    return if (char == null) {
        false
    } else {
        matches(char.toString())
    }
}

/* Tokenize input file */
class Lexer(private val input: String) {

    private fun isWhiteSpace(char: Char?): Boolean {
        return if (char == null) false else Regex("\\s").matches(char.toString())
    }

    /* 空白をスキップしてポインターを進める */
    private fun skipWhiteSpace() {
        while (!isEnd() && isWhiteSpace(peakChar())) {
            incP()
        }
        position = p
    }

    private fun peakChar(): Char? {
        return input.getOrNull(p)
    }

    /* pointer to current ch */
    private var p: Int = 0

    /* position after tokenized */
    private var position: Int = 0

    private fun readChar(): Char {
        val currentChar = input[p]
        incP()
        return currentChar
    }

    private fun incP(): Int {
        if (p < input.length) {
            p++
        }
        return p
    }

    private fun isEnd(): Boolean {
        return p == input.length
    }

    private fun ensureSetPosition(token: Token): Token {
        position = p
        return token
    }

    fun nextToken(): Token {
        skipWhiteSpace()
        if (isEnd()) return EOFToken()
        val startPosition = position
        return when (readChar()) {
            /* math */
            '+' -> ensureSetPosition(PlusToken())
            '-' -> ensureSetPosition(MinusToken())
            '*' -> ensureSetPosition(MultiToken())
            '/' -> ensureSetPosition(DivToken())
            /* Compare */
            '>' -> return when (peakChar()) {
                '=' -> {
                    incP()
                    ensureSetPosition(GrtEqToken())
                }
                else -> {
                    ensureSetPosition(GrtToken())
                }
            }
            '<' -> return when (peakChar()) {
                '=' -> {
                    incP()
                    ensureSetPosition(LssEqToken())
                }
                else -> {
                    ensureSetPosition(LssToken())
                }
            }
            '=' -> when (peakChar()) {
                '=' -> {
                    incP()
                    ensureSetPosition(EqualToken())
                }
                else -> {
                    ensureSetPosition(AssignToken())
                }
            }
            '!' -> {
                return when (peakChar()) {
                    '=' -> {
                        incP()
                        ensureSetPosition(NotEqToken())
                    }
                    else -> {
                        ensureSetPosition(BangToken())
                    }
                }
            }
            ',' -> ensureSetPosition(CommaToken())
            ';' -> ensureSetPosition(SemicolonToken())
            '(' -> ensureSetPosition(LParenToken())
            ')' -> ensureSetPosition(RParentToken())
            '{' -> ensureSetPosition(LBraceToken())
            '}' -> ensureSetPosition(RBraceToken())
            /* number */
            in Regex("[0-9]") -> {
                while (peakChar() in Regex("[0-9]")) {
                    incP()
                }
                ensureSetPosition(IntToken(input.slice(startPosition until p)))
            }
            in Regex("[a-zA-Z_]") -> {
                while (peakChar() in Regex("[a-zA-Z_0-9]")) {
                    incP()
                }
                when (val text = input.slice(startPosition until p)) {
                    /* keywords */
                    "function" -> ensureSetPosition(FuncToken(text))
                    "func" -> ensureSetPosition(FuncToken(text))
                    "fn" -> ensureSetPosition(FuncToken(text))
                    "return" -> ensureSetPosition(ReturnToken())
                    "let" -> ensureSetPosition(LetToken())
                    "val" -> ensureSetPosition(ValToken())
                    "var" -> ensureSetPosition(VarToken())
                    "true" -> ensureSetPosition(TrueToken())
                    "false" -> ensureSetPosition(FalseToken())
                    "if" -> ensureSetPosition(IfToken())
                    "when" -> ensureSetPosition(WhenToken())
                    "while" -> ensureSetPosition(WhileToken())
                    "do" -> ensureSetPosition(DoToken())
                    "const" -> ensureSetPosition(ConstToken())
                    "write" -> ensureSetPosition(WriteToken())
                    "writeln" -> ensureSetPosition(WritelnToken())
                    "begin" -> ensureSetPosition(BeginToken())
                    "end" -> ensureSetPosition(EndToken())
                    /* Identifier */
                    else -> ensureSetPosition(IdentifierToken(text))
                }
            }
            /* Illegal */
            else -> {
                incP()
                ensureSetPosition(IllegalToken(input.slice(startPosition until p)))
            }
            /* Op */
        }
    }

}
