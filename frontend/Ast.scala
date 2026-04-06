sealed trait Program
sealed trait Expression

// Binary arithmetic expressions
sealed trait BArithOp
case class Plus() extends BArithOp
case class Minus() extends BArithOp
case class Times() extends BArithOp
case class Div() extends BArithOp
case class Mod() extends BArithOp
case class LeftShift() extends BArithOp
case class RightShift() extends BArithOp

// Binary boolean expressions with boolean arguments
sealed trait BBop
case class And() extends BBop
case class Or() extends BBop
case class BEq() extends BBop
case class BNEq() extends BBop

// Unary boolean expressions
sealed trait UBop
case class Not() extends BBop

// Binary boolean expressions with arithmetic arguments
sealed trait ABop
case class Geq() extends BBop
case class Gt() extends BBop
case class Leq() extends BBop
case class AEq() extends BBop
case class ANEq() extends BBop
case class Lt() extends BBop

// Arithmetic Expressions
sealed trait AExpr
case class BaseInt(value: Int) extends AExpr
case class AOp(left: AExpr, op: BArithOp, right: AExpr) extends AExpr

// Boolean Expressions
sealed trait BExpr
case class True() extends BExpr
case class False() extends BExpr
case class BOpBool(left: BExpr, op: BBop, right: BExpr) extends BExpr
case class BopArith(left: BExpr, op: BBop, right: BExpr) extends BExpr

case class If()
case class Loop()
case class Fn()

sealed trait Expr