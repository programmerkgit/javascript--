package org.example.pl0

import org.example.*
import org.junit.Test
import org.junit.Assert.assertEquals

class TestLexer {
    @Test
    fun testConstDecl() {
        val l = Lexer(
            """
const a = 3;            
function a(b,c,d) {
    var e,f,g
};
""".trimIndent()
        )
        val tokens = listOf(
            ConstToken(),
            IdentifierToken("a"),
            AssignToken(),
            IntToken("3"),
            SemicolonToken(),
            FuncToken(),
            IdentifierToken("a"),
            LParenToken(),
            IdentifierToken("b"),
            CommaToken(),
            IdentifierToken("c"),
            CommaToken(),
            IdentifierToken("d"),
            RParentToken(),
            LBraceToken(),
            VarToken(),
            IdentifierToken("e"),
            CommaToken(),
            IdentifierToken("f"),
            CommaToken(),
            IdentifierToken("g"),
            RBraceToken(),
            SemicolonToken()
        )
        tokens.forEach {
            assertEquals(l.nextToken(), it)
        }
    }
}