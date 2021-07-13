package org.example.pl0

import org.junit.Test
import org.junit.Assert.assertEquals

/**
 * program := block .
 * block := [constDecl|varDecl|funcDecl] statement
 * constDecl := const identifier = number{, identifier = number } ;
 * varDecl := var identifier{, identifier};
 * funcDecl := function ident ([ident{, ident}]) {block};
 * statement := identifier = expression
 *              | begin statement{; statement} end
 *              | if ( condition ) then { statement }
 *              | while (condition) do { statement }
 *              | return expression
 *              | write expression
 *              | writeln
 * condition := expression =|<>|<|>|<=|>= expression
 * expression := [+|-] term {+ term}
 * term := factor {(*|/) factor }
 * factor := ident
 *          | number
 *          | ident ([expression{, expression}])
 *          | ( expression )
 *
 * */

class TestParser {

    @Test
    fun testConstDecl() {
        val l = Lexer("const a = 3")
        val p = Parser(l)
        p.parse()
        assertEquals(true, true)
    }

    @Test
    fun testVarDecl() {
        val l = Lexer("var id, id2, id3")
        val p = Parser(l)
        p.parse()
        assertEquals(true, true)
    }

    @Test
    fun testFuncDecl() {
        val l = Lexer("function a(e,b,c) {const a = 3}")
        val p = Parser(l)
        p.parse()
        assertEquals(true, true)
    }
}