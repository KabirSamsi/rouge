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
        '@' -> Tokens.InstanceFlag(),
        '$' -> Tokens.GlobalFlag(),
        ':' -> Tokens.Colon(),
        ';' -> Tokens.Semicolon(),
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
        "attr_accessor" -> Tokens.AttrAccessor(),
        "new" -> Tokens.New(),
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

    def turn_dec_float(number: scala.Float) : scala.Float = {
        if (number < 1) then return number else turn_dec_float (number/ 10)
    }
    
    def turn_dec(number: scala.Int) : scala.Float = {
        if (number < 1) then return number.toFloat else turn_dec_float (number.toFloat / 10)
    }
    
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
                
                if (isString) { // Complete string currently being built
                    if (h == '\"')
                    then lex(tokens.::(Tokens.Str(buffer)), "", false, false, tl)
                    else lex(tokens, buffer + h, true, false, tl)

                } else {

                    // Space/delineation in keywords -> append buffer
                    if (h == ' ' || h == '\n') {
                        if (keywordMappings.contains(buffer)) {
                            lex( // Add new token if there are keywords in buffer
                                if h == '\n' // Add line break for separators
                                then tokens.::(keywordMappings.apply(buffer)).::(Tokens.LineBreak())
                                else tokens.::(keywordMappings.apply(buffer)), "", false, h != '\n' && isComment, tl)
                        } else { 
                            if (h == '\n') { // Add line break for separators
                                lex((
                                    if buffer.isEmpty()
                                    then tokens
                                    else tokens.::(Tokens.Handle(buffer)))
                                .::(Tokens.LineBreak()), "", false, false, tl) // Only way to break out of a comment
                            } else {
                                lex(
                                    if buffer.isEmpty()
                                    then tokens
                                    else tokens.::(Tokens.Handle(buffer)), "", false, isComment, tl)
                            }
                        }

                    // Start building string
                    } else if (h == '"' && !isComment) {
                        lex(
                            if buffer.isEmpty()
                            then tokens
                            else tokens.::(Tokens.Handle(buffer)), "", true, false, tl)

                    // If new character is an individual token, add it along with buffer
                    } else if (singleCharMappings.contains(h) && !isComment) {
                        lex(
                            if buffer.isEmpty()
                            then tokens.::(singleCharMappings.apply(h))
                            else tokens.::(Tokens.Handle(buffer)).::(singleCharMappings.apply(h)), "", false, false, tl)

                    // Store new number if no string has been accumulated so far
                    } else if (h.isDigit && buffer.isEmpty() && !isComment) {
                        lex(tokens.::(Tokens.Int(h.asDigit)), "", false, false, tl)

                    // Comment heading – start disregarding til new line and don't accumulate.
                    } else if (h == '#' && !isComment) {
                        if (keywordMappings.contains(buffer)) { // Add remainder buffer
                            lex(tokens.::(keywordMappings.apply(buffer)), "", false, true, tl)
                        } else { 
                            lex(tokens, "", false, true, tl)
                        }

                    // If not comment and new character is not an individual token, continue to build buffer
                    } else {
                        lex(tokens, if isComment then buffer else buffer + h, false, isComment, tl)
                    }
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
            case Tokens.Greater() :: Tokens.Greater() :: tl => Tokens.RShift() :: lex_opt(tl)
            case Tokens.Less() :: Tokens.Less() :: tl => Tokens.LShift() :: lex_opt(tl)
            case Tokens.InstanceFlag() :: Tokens.InstanceFlag() :: tl => lex_opt(Tokens.ClassFlag() :: tl)

            /* Line Break Optimization for CFG Generation */
            case Tokens.LineBreak() :: Tokens.LineBreak() :: tl => lex_opt(Tokens.LineBreak() :: tl)
            case Tokens.Comma() :: Tokens.LineBreak() :: tl => lex_opt(Tokens.Comma() :: tl)
            case Tokens.LineBreak() :: Tokens.Dot() :: tl => lex_opt(Tokens.Dot() :: tl)

            /* Integer and float collection */
            case Tokens.Int(i1) :: Tokens.Int(i2) :: tl => lex_opt(Tokens.Int(i1 * 10 + i2) :: tl)
            case Tokens.Int(i1) :: Tokens.Dot() :: tl => {
                lex_opt(tl) match
                    case Tokens.Int(i2) :: tl2 => Tokens.Float(i1 + turn_dec(i2)) :: tl2
                    case Tokens.Float(i2) :: tl2 => Tokens.Float(i1 + turn_dec_float(i2)) :: tl2
                    case tl2 => Tokens.Int(i1) :: tl2
            }

            /* Character and string collection */
            case Tokens.SingleQuote() :: Tokens.Handle(c) :: Tokens.SingleQuote() :: tl => {
                if (c.length() == 1)
                then Tokens.Character(c.charAt(0)) :: lex_opt(tl)
                else Tokens.SingleQuote() :: Tokens.Handle(c) :: Tokens.SingleQuote() :: lex_opt(tl)
            }

            /* Typing and objects */
            case Tokens.Colon() :: Tokens.Colon() :: tl => Tokens.Doublecolon() :: lex_opt(tl)
            case Tokens.Handle(v) :: Tokens.Colon() :: Tokens.Handle(t) :: tl => Tokens.TypedHandle(v, t) :: lex_opt(tl)
            case Tokens.InstanceFlag() :: Tokens.Handle(v) :: tl => Tokens.InstanceVar(v) :: lex_opt(tl)
            case Tokens.ClassFlag() :: Tokens.Handle(v) :: tl => Tokens.ClassVar(v) :: lex_opt(tl)
            case Tokens.GlobalFlag() :: Tokens.Handle(v) :: tl => Tokens.GlobalVar(v) :: lex_opt(tl)
            case Tokens.Handle(v) :: tl => (
                if v.equals(v.toUpperCase())
                then Tokens.Const(v)
                else Tokens.Handle(v)) :: lex_opt(tl)

            /* Default case */
            case hd :: tl => hd :: lex_opt(tl)
    }
}
