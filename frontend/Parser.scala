package frontend

import scala.util.Using
import scala.io.Source
import scala.compiletime.ops.int
import scala.compiletime.ops.boolean

object CFG {

  def parseIfExpr(tokens : List[Tokens.TypedVar], unless : boolean) : Ast.IfElse {
  }
  
  /* Parses a stream of typed arguments */
  def parseTypedVars(acc : List[Ast.TypedVar], tokens : List[Tokens.Token]) : (List[Ast.TypedVar], List[Tokens.Token]) = {
    tokens match {
      case Tokens.Colon() :: Tokens.TypedHandle(n, s) :: Tokens.Comma() :: tl => parseTypedVars(Ast.TypedVar(n, s) :: acc, tl)
      case Tokens.Colon() :: Tokens.TypedHandle(n, s) :: tl => (Ast.TypedVar(n, s) :: acc, tl)
      case Tokens.TypedHandle(n, s) :: Tokens.Comma() :: tl => parseTypedVars(Ast.TypedVar(n, s) :: acc, tl)
      case Tokens.TypedHandle(n, s) :: Tokens.RParen() :: tl => (Ast.TypedVar(n, s) :: acc, tl)
    }
  }

  def parseFnApp(name : string, tokens : List[Tokens.Token]) : Ast.Expr = {

  }

  def parseFunctionBody(name : string, acc : List[Ast.Expr], tokens : List[Tokens.Token]) : List[Ast.Expr] = {
    tokens match
      case Tokens.End() :: tl => (Ast.Function(name, params, returnType, body), tl)
      case Tokens.LineBreak() :: tl => parseFunctionBody(name, acc, tl)

      case Tokens.Handle(v) :: Tokens.Equals() :: tl => { // Variable Assignment
        val (assignExpr, newTl) = parseAssign(v, tl)
        parseFunctionBody(v, assignExpr :: acc, newTl)
      }
      case Tokens.If() :: tl => { // If statement
        val (ifExpr, newTl) = parseIfExpr(tl, false)
        parseFunctionBody(v, ifExpr :: acc, newTl)
      }
      case Tokens.Unless() :: tl => { // Unless statement
        val (ifExpr, newTl) = parseIfExpr(tl, true)
        parseFunctionBody(v, ifExpr :: acc, newTl)
      }

      case Handle(f) :: LParen() :: tl => { // Function application
        val (fnApp, newTl) = parseFnApply(f, tl)
        parseFunctionBody(fnApp :: acc, newTl)
      }

      case Tokens.Handle(v) :: Tokens.Dot() :: Tokens.Handle(f) :: tl => { // Method call
        val (fnApp, newTl) = parseFnApply(f, tl)
        parseFunctionBody(Ast.MethodCall(v, fnApp) :: acc, newTl)
      }

      case Tokens.New() :: Tokens.Handle(c) :: Tokens.LParen() :: tl => {
        val (fnApp, newTl) = parseFnApply(f, tl)
        parseFunctionBody(Ast.InstantiateObject(fnApp) :: acc, newTl)
      }
  }
  
  def parseFunction(name: string, params: List[Ast.TypedVar], returnType : string, body : Ast.Program) : (Ast.Function, List[Tokens.Token]) = {
    tokens match {
      case Token.LParen() :: tl => {
        val (params : List[Ast.TypedVar], newTail: List[Tokens.Token]) = parseTypedVars(tl)
        parseFunction(name, params, returnType, body, newTail)
      }
      // Parse return type
      case Token.Colon :: Token.Handle(v) :: tl => parseFunctionBody(tl)
      case _ => {
        val (parsedBody, newtl) = parseFunctionBody(Nil, tokens)
        parseFunction(name, params, returnType, parsedBody)
      }
    }
  }
  
  def parseClass(name: string, classVars: List[Ast.TypedVar], methods: List[Ast.Function], tokens : List[Tokens.Token]) : (Ast.Class, List[Tokens.Token]) = {
    tokens match {
      case Token.AttrAccessor() :: tl => {
        val (classVars : List[Ast.TypedVar], newTail: List[Tokens.Token]) = parseTypedVars(tl)
        parseClass(name, instanceVars, methods, newTail)
      }

      case Token.Def() :: Token.Handle(v) => {
        val (parsedFunction, newTail) = parseFunction(v )
      }

      case End :: tl => (Ast.Class(name, classVars, methods), tl)
    }
  }
  
  def parseProgram(classes : List[Ast.Class], functions: List[Ast.Function], body : List[Ast.Exp], tokens : List[Tokens.Token]) : Ast.Program = {
    tokens match {
      case Nil => Ast.Program(classes, functions, body)
      case Token.Class() :: Token.Handle(v) :: tl => {
        val (parsedClass, newTail) = parseClass(v, Nil, Nil, tl)
        parseProgram(parsedClass :: classes, functions, body, newTail)
      }
      case Token.Function() :: Token.Handle(v) :: tl => {
        val (parsedFunction, newTail) = parseFunction(v, tl)
        parseProgram(classes, parsedFunction :: functions, body, newTail)
      }
    }
  }

  def main(args: Array[String]) : Unit = {
    val filename = args(0)
    val contents: String = Using(Source.fromFile(filename))(_.mkString).get
    val tokens : List[Tokens.Token] =  Lexer.lex_opt(Lexer.lex(Nil, "", false, false, contents.toList))
    val ast : Ast.Program = parse_program(tokens)
    println(tokens)
  }
}