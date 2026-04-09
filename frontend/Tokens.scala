package frontend

object Tokens {
    // Ruby tokens
    sealed trait Token

    // Misc
    case class UBegin() extends Token
    case class UEnd() extends Token
    case class Alias() extends Token
    case class Puts() extends Token
    case class Redo() extends Token
    case class Rescue() extends Token
    case class Retry() extends Token
    case class Yield() extends Token
    case class Undef() extends Token
    case class RNil() extends Token
    
    // Classes and Objects
    case class Class() extends Token
    case class Super() extends Token
    case class Self() extends Token
    case class AttrAccessor() extends Token
    case class New() extends Token

    case class Module() extends Token

    // Bracket Keywords
    case class LParen() extends Token
    case class RParen() extends Token
    case class LBracket() extends Token
    case class RBracket() extends Token
    case class LBrace() extends Token
    case class RBrace() extends Token

    // Control Flow Keywords
    case class If() extends Token
    case class Elsif() extends Token
    case class Else() extends Token
    case class Then() extends Token
    case class Unless() extends Token
    case class While() extends Token
    case class Until() extends Token
    case class Do() extends Token
    case class Break() extends Token
    case class Case() extends Token
    case class Begin() extends Token
    case class End() extends Token

    // Booleans
    case class True() extends Token
    case class False() extends Token
    case class And() extends Token
    case class Or() extends Token
    case class Not() extends Token

    // Function Keywords
    case class Def() extends Token
    case class Defined() extends Token
    case class Return() extends Token

    // Containment
    case class For() extends Token
    case class In() extends Token

    // Operators
    case class Plus() extends Token
    case class Minus() extends Token
    case class Times() extends Token
    case class Divide() extends Token
    case class Mod() extends Token
    case class LShift() extends Token
    case class RShift() extends Token
    case class Equals() extends Token
    case class Greater() extends Token
    case class Less() extends Token
    case class Bang() extends Token
    case class Ternary() extends Token
    case class InstanceFlag() extends Token
    case class ClassFlag() extends Token
    case class GlobalFlag() extends Token
    
    // Datatypes
    case class Handle(value: String) extends Token
    case class TypedHandle(value: String, typ: String) extends Token
    case class InstanceVar(value: String) extends Token
    case class ClassVar(value: String) extends Token
    case class GlobalVar(value: String) extends Token
    case class Const(value: String) extends Token
    case class Str(value: String) extends Token
    case class Int(value: scala.Int) extends Token
    case class Float(value: scala.Float) extends Token
    case class Character(value: scala.Char) extends Token

    case class Peq() extends Token
    case class Meq() extends Token
    case class Teq() extends Token
    case class Deq() extends Token
    case class Modeq() extends Token
    case class Equality() extends Token
    case class Treq() extends Token
    case class Geq() extends Token
    case class Leq() extends Token
    case class Neq() extends Token

    case class Colon() extends Token
    case class Semicolon() extends Token
    case class Dot() extends Token
    case class Comma() extends Token
    case class Doublecolon() extends Token

    // Escapes
    case class DoubleQuote() extends Token
    case class SingleQuote() extends Token
    case class Backslash() extends Token
    case class LineBreak() extends Token
}
