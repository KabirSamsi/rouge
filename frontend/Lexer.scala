package frontend

import scala.util.Using
import scala.io.Source

object Lexer {

    // Maps individual characters to tokens
    val singleCharMappings : Map[Char, Tokens.Token] = Map(
        '+' -> Tokens.Plus(),
        '-' -> Tokens.Minus(),
        '*' -> Tokens.Times(),
        '/' -> Tokens.Divide(),
        '=' -> Tokens.Equals(),
        '>' -> Tokens.Greater(),
        '<' -> Tokens.Less(),
        '!' -> Tokens.Bang(),
        '%' -> Tokens.Mod(),
        '?' -> Tokens.Ternary(),
        ':' -> Tokens.Colon(),
        '\\' -> Tokens.Backslash(),
        '"' -> Tokens.DoubleQuote(),
        '\'' -> Tokens.SingleQuote(),
        '(' -> Tokens.LParen(),
        ')' -> Tokens.RParen(),
        '[' -> Tokens.LBracket(),
        ']' -> Tokens.RBracket(),
        '{' -> Tokens.LBrace(),
        '}' -> Tokens.RBrace(),
        '.' -> Tokens.Dot(),
        ',' -> Tokens.Comma(),
        '\n' -> Tokens.LineBreak()
    )

    // Maps keywords to tokens
    val keywordMappings : Map[String, Tokens.Token] = Map(
        "BEGIN" -> Tokens.UBegin(),
        "END" -> Tokens.UEnd(),
        "alias" -> Tokens.Alias(),
        "puts" -> Tokens.Puts(),
        "redo" -> Tokens.Redo(),
        "rescue" -> Tokens.Rescue(),
        "retry" -> Tokens.Retry(),
        "yield" -> Tokens.Yield(),
        "undef" -> Tokens.Undef(),
        "nil" -> Tokens.RNil(),
        "class" -> Tokens.Class(),
        "super" -> Tokens.Super(),
        "self" -> Tokens.Self(),
        "module" -> Tokens.Module(),
        "if" -> Tokens.If(),
        "elsif" -> Tokens.Elsif(),
        "else" -> Tokens.Else(),
        "then" -> Tokens.Then(),
        "unless" -> Tokens.Unless(),
        "while" -> Tokens.While(),
        "until" -> Tokens.Until(),
        "do" -> Tokens.Do(),
        "break" -> Tokens.Break(),
        "case" -> Tokens.Case(),
        "begin" -> Tokens.Begin(),
        "end" -> Tokens.End(),
        "true" -> Tokens.True(),
        "false" -> Tokens.False(),
        "and" -> Tokens.And(),
        "or" -> Tokens.Or(),
        "not" -> Tokens.Not(),
        "def" -> Tokens.Def(),
        "defined" -> Tokens.Defined(),
        "return" -> Tokens.Return(),
        "for" -> Tokens.For(),
        "in" -> Tokens.In()
    )
    
    // Main lexing body
    def lex(tokens : List[Tokens.Token], buffer: String, isString : Boolean,
            isComment: Boolean, characters : List[Char]) : List[Tokens.Token] = {

        characters match
            // Empty sequence -> return token sequence
            case Nil =>
                if buffer.isEmpty()
                then tokens
                else tokens.::(Tokens.Handle(buffer))

            case h :: tl => {
                // Space/delineation in keywords -> append buffer
                if (h == ' ' || h == '\n') {
                    if (keywordMappings.contains(buffer)) {
                        lex(tokens.::(keywordMappings.apply(buffer)), "", false, false, tl)
                    } else {
                        // Add new token if there are keywords in buffer
                        lex(
                            if buffer.isEmpty()
                            then tokens
                            else tokens.::(Tokens.Handle(buffer)), "", false, false, tl)
                    }

                // If new character is an individual token, add it along with buffer
                } else if (singleCharMappings.contains(h)) {
                    lex(
                        if buffer.isEmpty()
                        then tokens.::(singleCharMappings.apply(h))
                        else tokens.::(Tokens.Handle(buffer)).::(singleCharMappings.apply(h)), "", false, false, tl)

                // If new character is not an individual token, continue to build buffer
                } else {
                    lex(tokens, buffer + h, false, false, tl)
                }
            }
    }
}
