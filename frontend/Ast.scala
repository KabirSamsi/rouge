package frontend

object Ast {
    
    case class Type(name: String)
    
    // Arithmetic Operators
    sealed trait Aop
    case class Plus() extends Aop
    case class Minus() extends Aop
    case class Times() extends Aop
    case class Divide() extends Aop
    case class Mod() extends Aop
    case class LShift() extends Aop
    case class RShift() extends Aop
    
    // Comparison operators that return booleans
    sealed trait CompOp
    case class Eq() extends CompOp
    case class Treq() extends CompOp
    case class Gt() extends CompOp
    case class Lt() extends CompOp
    case class Geq() extends CompOp
    case class Leq() extends CompOp
    case class Neq() extends CompOp

    // Boolean Operands
    sealed trait Bop
    case class And() extends Bop
    case class Or() extends Bop
    
    sealed trait Exp
    
    // Arithmetic Comparison Operators
    sealed trait AExp extends Exp
    case class RbInt(n : scala.Int) extends AExp
    case class RbFlt(n: scala.Float) extends AExp
    case class ArithBin(a1: AExp, a2: AExp, op: Aop) extends AExp

    // Boolean Expressions
    sealed trait BExp extends Exp
    case class True() extends BExp
    case class False() extends BExp
    case class BoolBin(b1: Exp, a2: Exp, op: CompOp) extends BExp
    case class BoolNot(a : BExp) extends BExp

    // Identifiers
    sealed trait Identifier extends Exp
    case class TypedVar(name : String, typ: Type) extends Identifier
    case class ClassVar(name: String) extends Identifier
    case class InstanceVar(name: String) extends Identifier
    case class ObjectName(name: String) extends Identifier
    case class ClassName(name: String) extends Identifier
    
    case class IfElse(guards: List[BExp], branches : List[Exp]) extends Exp
    case class Unless(guard: BExp, b1 : Exp, b2 : Option[Exp]) extends Exp

    // Update variables
    sealed trait WriteToVar extends Exp
    case class Assign(name: Identifier, contained : Exp) extends WriteToVar
    case class Peq(name: Identifier, contained : AExp) extends WriteToVar
    case class Meq(name: Identifier, contained : AExp) extends WriteToVar
    case class Teq(name: Identifier, contained : AExp) extends WriteToVar
    case class Deq(name: Identifier, contained : AExp) extends WriteToVar
    case class Modeq(name: Identifier, contained : AExp) extends WriteToVar
    case class AndEq(name: Identifier, contained : BExp) extends WriteToVar
    case class OrEq(name: Identifier, contained : BExp) extends WriteToVar

    // Data Structures
    sealed trait Iterable extends Exp
    case class RbString(contained: String) extends Iterable
    case class RbArray(components: List[Exp]) extends Iterable
    case class RbDictionary(keys: List[Exp], values : List[Exp]) extends Iterable
    case class RbRange(rStart : Int, rEnd : Int) extends Iterable

    // Large-Level Constructs
    case class FnApply(name: String, args: List[Exp]) extends Exp
    case class MethodCall(obj : ObjectName, fn : FnApply) extends Exp
    case class InstantiateObject(fn : FnApply) extends Exp
    case class DotApply(obj: Exp, method: FnApply) extends Exp
    case class While(guard: BExp, body: List[Exp]) extends Exp
    case class ForEach(iterator : String, iterable: Iterable, body: List[Exp]) extends Exp

    // Largest components
    case class Function(name: String, params: List[TypedVar], returnType : Type, body : List[Exp]) extends Exp
    case class Class(name: String, classVars: List[ClassVar], methods: List[Function]) extends Exp
    case class Module(name: String, instanceVars: List[TypedVar], methods: List[Function]) extends Exp

    case class Program(classes : List[Class], functions: List[Function], body : List[Exp])
}