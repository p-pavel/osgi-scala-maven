package com.perikov.cassandra.macros

import scala.quoted.*

extension (expr: Any)
  transparent inline def dispatchTo[T](interpreter: T) =
    generateDispatch(expr, interpreter)

// TODO: I hate the amount of code here and duplication of concepts.
// Probably use something like term deriving on annotation type to
// find the implementation of generator

transparent inline def generateDispatch[T](e: Any, interpreter: T) = ${
  traitAnnotationsImpl[T]('e, 'interpreter)
}

extension (s: String)
  inline def dispatcherByMethodName[T](interpreter: T) = ${
    dispatchByMethodNameImpl[T]('s, 'interpreter)
  }

def dispatchByMethodNameImpl[T: Type](s: Expr[String], interpreter: Expr[T])(
    using Quotes
) =
  DispatchGenerator(s, interpreter).generateDispatchByMethodName

def traitAnnotationsImpl[T: Type](e: Expr[Any], interpreter: Expr[T])(using
    Quotes
) =
  DispatchGenerator(e, interpreter).generateDispatch

private class DispatchGenerator[T: Type](
    selector: Expr[Any],
    val interpreter: Expr[T]
)(using Quotes) extends TraitUtils[T]:
  import quotes.reflect.*

  val interpreterTerm            = interpreter.asTerm

  def generateDispatchByMethodName =
    val dispatchBySymbol = TypeRepr.of[dispatchByMethodName].typeSymbol

    val annotatedBaseClassess = typeRepr.baseClasses.filter(
      _.annotations.exists(_.tpe.typeSymbol == dispatchBySymbol)
    )
    val methodsToDispatchBy = annotatedBaseClassess.flatMap(_.declaredMethods)
    if methodsToDispatchBy.isEmpty then
      report.errorAndAbort(
        s"I didn't find any method to dispatch by for $targetSymbol. Probably you forgot @${dispatchBySymbol} annotation"
      )
    val cases               = methodsToDispatchBy.map(m =>
      CaseDef(
        Literal(StringConstant(m.name)),
        None,
        callWithGivensImpl(interpreterTerm.select(m).etaExpand(Symbol.spliceOwner).asExpr).asTerm
      )
    )
    Match(selector.asTerm, cases).asExpr
  end generateDispatchByMethodName

  def generateDispatch: Expr[Any] =

    val dispatchBySymbol        = TypeRepr.of[dispatchBy].typeSymbol
    val annotations: List[Term] = annotationsWith(dispatchBySymbol)

    if annotations.length != 1 then
      report.errorAndAbort(
        s"Expected exactly one annotation of type `dispatchBy` for $targetSymbol, found ${annotations.length}"
      )

    val symbolToDispatchBy = annotations.head.tpe.typeArgs.head.typeSymbol

    val cases = annotatedMethods(symbolToDispatchBy).map((methodSymbol, pattern) =>
      CaseDef(
        pattern,
        None,
        callWithGivensImpl(
          try
            interpreterTerm
              .select(methodSymbol)
              .etaExpand(Symbol.spliceOwner)
              .asExpr
          catch
            case e: Exception =>
              report.errorAndAbort(
                s"Failed to select method ${methodSymbol.name} from interpreter: ${interpreterTerm}:\n${e.getMessage()}"
              )
        ).asTerm
      )
    )
    Match(selector.asTerm, cases).asExpr
  end generateDispatch

end DispatchGenerator
