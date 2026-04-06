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
    
    /*
        * Lexes a program string into a stream of tokens.
        * @param tokens – List of existing lexed tokens
        * @param buffer – Currently accumulating string to be tokenized
        * @param isString – Tracks whether current buffer stores a Ruby string (i.e. within quotes)
        * @param isString – Tracks whether current buffer stores a Ruby comment (i.e. prefixed with #)
        * @param characters – Stores remaining (un-tokenized) characters in program string.
        * @return Stream of lexed tokens
    */
    def lex(tokens : List[Tokens.Token], buffer: String, isString : Boolean,
            isComment: Boolean, characters : List[Char]) : List[Tokens.Token] = {

        characters match
            // Empty sequence -> return token sequence
            case Nil =>
                if buffer.isEmpty()
                then tokens.reverse
                else tokens.::(Tokens.Handle(buffer)).reverse

            case h :: tl => {
                // Space/delineation in keywords -> append buffer
                if (h == ' ' || h == '\n') {
                    if (keywordMappings.contains(buffer)) {
                        lex(
                            if h == '\n' // Add line break for separators
                            then tokens.::(keywordMappings.apply(buffer)).::(Tokens.LineBreak())
                            else tokens.::(keywordMappings.apply(buffer)), "", false, false, tl)
                    } else { // Add new token if there are keywords in buffer
                        if (h == '\n') { // Add line break for separators
                            lex((
                                if buffer.isEmpty()
                                then tokens
                                else tokens.::(Tokens.Handle(buffer)))
                            .::(Tokens.LineBreak()), "", false, false, tl)
                        } else {
                            lex(
                                if buffer.isEmpty()
                                then tokens
                                else tokens.::(Tokens.Handle(buffer)), "", false, false, tl)
                        }
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

    
    /*
        * Optimizes token stream through operator fusion, integer accumulation, line breaks, etc.
        * @param tokens – Stream of tokens
        * @return Optimized stream of tokens
    */
    def lex_opt(tokens : List[Tokens.Token]) : List[Tokens.Token] = {
        tokens match
            case Nil => Nil

            /* Operator Fusion */
            case Tokens.Plus() :: Tokens.Equals() :: tl => Tokens.Peq() :: lex_opt(tl)
            case Tokens.Minus() :: Tokens.Equals() :: tl => Tokens.Meq() :: lex_opt(tl)
            case Tokens.Times() :: Tokens.Equals() :: tl => Tokens.Teq() :: lex_opt(tl)
            case Tokens.Divide() :: Tokens.Equals() :: tl => Tokens.Deq() :: lex_opt(tl)
            case Tokens.Mod() :: Tokens.Equals() :: tl => Tokens.Modeq() :: lex_opt(tl)
            case Tokens.Greater() :: Tokens.Equals() :: tl => Tokens.Geq() :: lex_opt(tl)
            case Tokens.Less() :: Tokens.Equals() :: tl => Tokens.Leq() :: lex_opt(tl)
            case Tokens.Equals() :: Tokens.Equals() :: tl => Tokens.Equality() :: lex_opt(tl)
            case Tokens.Equality() :: Tokens.Equals() :: tl => Tokens.Treq() :: lex_opt(tl)
            case Tokens.Bang() :: Tokens.Equals() :: tl => Tokens.Neq() :: lex_opt(tl)

            /* Line Break Optimization for CFG Generation */
            case Tokens.LineBreak() :: Tokens.LineBreak() :: tl => lex_opt(Tokens.LineBreak() :: tl)
            case Tokens.Comma() :: Tokens.LineBreak() :: tl => lex_opt(Tokens.Comma() :: tl)
            case Tokens.LineBreak() :: Tokens.Dot() :: tl => lex_opt(Tokens.Dot() :: tl)

            /* Integer and float collection */
            case Tokens.Int(i1) :: Tokens.Int(i2) :: tl => lex_opt(Tokens.Int(i1 * 10 + i2) :: tl)
            case hd :: tl => hd :: lex_opt(tl)
    }
}
