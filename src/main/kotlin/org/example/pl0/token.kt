package org.example.pl0

enum class TokenKind {
    /* Special tokens */
    EOF, //end of file
    ILLEGAL, // not recognized
    LOWEST, // dummy

    /* Literal */
    IDENTIFIER, //
    INT, //

    /* Delimiters */
    LPAREN,//(
    RPAREN,//)
    LBRACE, // {
    RBRACE, // }
    COMMA, //,
    SEMICOLON, //;
    PERIOD, //.

    /* Math */
    PLUS, // +
    MINUS, // -
    MULTI, // *
    DIV, // /
    BANG, // !

    /* Compare */
    LSS, //<
    GRT, //>
    LSSEQ, //<=
    GRTEQ, //>=
    EQUAL, // ==
    NOTEQ, // !=

    /* KEYWORDS */

    /* Function */
    FUNCTION, // fn,
    RETURN, // return

    /* DEC */
    VAR, //var
    LET, //let
    CONST, //const
    VAL, // val
    ASSIGN, // =

    /* Sentences */
    IF, // if
    ELSE, // else
    WHILE, // while,
    WHEN, //when,
    THEN, // then

    /* Bool */
    TRUE, //true
    FALSE, // false

    /* Others */
    BEGIN, // begin
    END, // end
    WRITE, // write
    WRITELN, // writeln,
    DO, // do
}

open class Token(
    val kind: TokenKind,
    val literal: String
) {
    override fun toString(): String {
        return "{Kind: ${kind}: Literal: $literal}"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Token) {
            kind == other.kind && literal == other.literal
        } else {
            false
        }
    }
}

class IllegalToken(value: String) : Token(TokenKind.ILLEGAL, value)
class EOFToken() : Token(TokenKind.EOF, "")
class IdentifierToken(value: String) : Token(TokenKind.IDENTIFIER, value)
class IntToken(value: String) : Token(TokenKind.INT, value)

/* Math */
class PlusToken() : Token(TokenKind.PLUS, "+")
class MinusToken() : Token(TokenKind.MINUS, "-")
class MultiToken() : Token(TokenKind.MULTI, "*")
class DivToken() : Token(TokenKind.DIV, "/")

/* Prefix Op */
class BangToken() : Token(TokenKind.BANG, "!")

/* Assign Op */
class AssignToken() : Token(TokenKind.ASSIGN, "=")

/* DelimiterToken */
class LParenToken() : Token(TokenKind.LPAREN, "(")
class RParentToken() : Token(TokenKind.RPAREN, ")")
class LBraceToken() : Token(TokenKind.LBRACE, "{")
class RBraceToken() : Token(TokenKind.RBRACE, "}")
class CommaToken() : Token(TokenKind.COMMA, ",")
class SemicolonToken() : Token(TokenKind.SEMICOLON, ";")
class PeriodToken() : Token(TokenKind.PERIOD, ".")

/* Comp */
class EqualToken() : Token(TokenKind.EQUAL, "==")
class NotEqToken() : Token(TokenKind.NOTEQ, "!=")
class LssToken() : Token(TokenKind.LSS, "<")
class LssEqToken() : Token(TokenKind.LSSEQ, "<=")
class GrtToken() : Token(TokenKind.GRT, ">")
class GrtEqToken() : Token(TokenKind.GRTEQ, ">=")

/* keywords */
class FuncToken(literal: String = "function") : Token(TokenKind.FUNCTION, literal)
class IfToken() : Token(TokenKind.IF, "if")
class WhenToken() : Token(TokenKind.WHEN, "when")
class ElseToken() : Token(TokenKind.ELSE, "else")
class ThenToken() : Token(TokenKind.THEN, "then")
class ReturnToken() : Token(TokenKind.RETURN, "return")

abstract class BoolToken(kind: TokenKind, literal: String) : Token(kind, literal)
class TrueToken() : BoolToken(TokenKind.TRUE, "true")
class FalseToken() : BoolToken(TokenKind.FALSE, "false")

/* Assign Keyword */
class LetToken() : Token(TokenKind.LET, "let")
class ValToken() : Token(TokenKind.VAL, "val")
class VarToken() : Token(TokenKind.VAR, "var")
class ConstToken() : Token(TokenKind.CONST, "const")

/* other keyword */
class WriteToken() : Token(TokenKind.WRITE, "write")
class WritelnToken() : Token(TokenKind.WRITELN, "writeln")
class BeginToken() : Token(TokenKind.BEGIN, "begin")
class EndToken() : Token(TokenKind.END, "end")
class WhileToken() : Token(TokenKind.WHILE, "while")
class DoToken() : Token(TokenKind.DO, "do")