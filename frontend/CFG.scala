package frontend

import scala.util.Using
import scala.io.Source

object CFG {
  
  def main(args: Array[String]) : Unit = {
    val filename = args(0)
    val contents: String = Using(Source.fromFile(filename))(_.mkString).get
    val tokens : List[Tokens.Token] =  Lexer.lex_opt(Lexer.lex(Nil, "", false, false, contents.toList))
    println(tokens)
  }
}