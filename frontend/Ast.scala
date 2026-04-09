package frontend

object Ast {

    sealed trait TypedVar(name : string, typ: string)
    
    // Arithmetic Operators
    sealed trait Aop()
    case class Plus() extends Aop
    case class Minus() extends Aop
    case class Times() extends Aop
    case class Divide() extends Aop
    case class Mod() extends Aop
    case class LShift() extends Aop
    case class RShift() extends Aop
    case class Greater() extends Aop
    case class Peq() extends Aop
    case class Meq() extends Aop
    case class Teq() extends Aop
    case class Deq() extends Aop
    case class Modeq() extends Aop
    case class Aeq() extends Aop
    case class Treq() extends Aop
    case class Geq() extends Aop
    case class Leq() extends Aop
    case class Neq() extends Aop

    // Boolean Operands
    case class True() extends Bop
    case class False() extends Bop
    case class And() extends Bop
    case class Or() extends Bop
    case class Not() extends Bop
    
    sealed trait Exp()
    
    // Arithmetic Comparison Operators
    case class Integer(n : int) extends Exp
    case class Float(n: float) extends Exp
    case class ArithBin(a1: AExp, a2: AExp, op: Aop) extends Exp

    // Boolean Comparison Operators
    case class BoolUn(a : BExp, op: Aop) extends Exp
    case class BoolBin(b1: BExp, a2: BExp, op: Bop) extends Exp
    case class ArithEq(a: AExp, a2: AExp, op: Bop) extends Exp
    
    // Standard Expression
    case class AssignVar(name: TypedVar, contained : Exp) extends Statement
    case class Identifier(name: string) extends Exp
    case class IfElse(guard: BExp, body : List[Expression]) extends Exp

    // Data Structures
    sealed trait Iterable extends Exp
    case class Array(components: List[Expression]) extends Iterable
    case class Dictionary(Keys: List[Identifier], Values : List[Expression]) extends Iterable
    case class Range(Start : Int, End : Int) extends Iterable

    // Large-Level Constructs
    case class Function(name: string, params: List[TypedVar], returnType : TypedVar, body : List[Exp]) extends Exp
    case class FnApply(name: string, args: List[Exp]) extends Exp
    case class DotApply(obj: Exp, method: FnApply) extends Exp
    case class While(guard: BExp, body: List[Exp]) extends Exp
    case class ForEach(iterator : string, iterable: Iterable, body: List[Exp]) extends Exp
    case class Class(name: string, instanceVars: List[TypedVar], methods: List[Function]) extends Exp
    case class Module(name: string, instanceVars: List[TypedVar], methods: List[Function]) extends Exp
}