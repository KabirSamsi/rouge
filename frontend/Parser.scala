package frontend

import scala.util.Using
import scala.io.Source

object CFG {

  // Components
  def peek(tokenType : Token, tokens: List[Tokens.Token]) : Option[Token] = {
    tokens match
      case Nil => None
      case hd :: tl => if hd = tokenType then true else None
  }

  def expect(tokenType : Token, tokens : List[Tokens.Token]) : Option[Token] = {
    tokens match
      case Nil => None
      case hd :: tl => if hd = tokenType then hd else None
  }

  def main(args: Array[String]) : Unit = {
    val filename = args(0)
    val contents: String = Using(Source.fromFile(filename))(_.mkString).get
    val tokens : List[Tokens.Token] =  Lexer.lex_opt(Lexer.lex(Nil, "", false, false, contents.toList))
    println(tokens)
  }
}