package com.perikov.cassandra.macros

import scala.quoted.*

/** Having something resembling function generate a call to it with parameters
  * derived from givens in scope
  *
  * @param t
  *   function or method
  * @return
  *   the result of function call
  * 
  * @note
  *   If the argument is not a function or method, it will be returned as is.
  *   This is also true for functions that do not have parameters as they are
  *   evaluated at the call site.
  */
transparent inline def callWithGivens(t: Any) = ${ callWithGivensImpl('t) }

private [macros] def callWithGivensImpl(func: Expr[Any])(using Quotes): Expr[Any] =
  import quotes.*
  import quotes.reflect.*

  val term = func.asTerm
  val tpe  = term.tpe.widen
  if (!tpe.isFunctionType) then func
  else
    def findImplicit(arg: TypeRepr): Term =
      Implicits.search(arg) match
        case iss: ImplicitSearchSuccess => iss.tree
        case isf: ImplicitSearchFailure =>
          report.errorAndAbort(
            s"Could not find implicit of type '${arg.show(using Printer.TypeReprAnsiCode)}' for parameter in ${func.show}: ${tpe}"
          )

    val argsWithoutReturnType = tpe.typeArgs.dropRight(1).map(findImplicit)
    val applySymbol          = tpe.typeSymbol.methodMember("apply")
    assert(
      applySymbol.length == 1,
      s"Expected exactly one 'apply' method for function ${func.show}"
    )
    val application           =
      term.select(applySymbol.head).appliedToArgs(argsWithoutReturnType)
    application.asExpr
