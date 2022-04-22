package org.example.pl0

import org.junit.Test

/**
 * program := block
 * block := statement-list
 * statement-list := {statement}
 * statement := identifier = expression
 *              | if "(" condition ")" "{" statement-list "}"
 *              | while "("condition")" do "{" statement-list "}"
 *              | return expression
 *              | write expression
 *              | writeln
 *              | constDecl
 *              | varDecl
 *              | funcDecl
 * constDecl := const identifier = number{, identifier = number }
 * varDecl := var identifier{, identifier}
 * funcDecl := function ident "("[ident{, ident}]")" "{" block "}";
 * condition := expression =|<>|<|>|<=|>= expression
 * expression := [+|-] term {+ term}
 * term := factor {(*|/) factor }
 * factor := ident
 *          | number
 *          | ident "("[expression{, expression}]")"
 *          | "(" expression ")"
 *
 */


class TestExecutor {
    @Test
    fun testExecutor1() {
        val l = Lexer(
            """
function a(x, y) {
    begin
        write x;
        write y;
    end
}
begin
    write a(2,3)
end
                
        """.trimIndent()
        )
        val p = Parser(l)
        val codes = p.parse()
        Executor(codes).execute()
    }

    @Test
    fun testExecutor2() {
        val l = Lexer(
            """
                
                
write 3
writeln
function fibonacci(n) { 
    if(n == 0) {
        return 0
    }
    if (n == 1) {
        return 1 
    }
    return fibonacci(n -1) + fibonacci(n - 2)
}
var i
i = 0

while(i < 10) do {
    write fibonacci(i) 
    writeln
    i = i + 1
}
""".trimIndent()
        )
        val p = Parser(l)
        val codes = p.parse()
        Executor(codes).execute()
    }

}