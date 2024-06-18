package com.perikov.cassandra.macros

import scala.quoted.*

trait TraitUtils[T: Type](using q: Quotes):
  protected val quotes = q
  import quotes.reflect.*

  protected lazy val typeRepr     = TypeRepr.of[T].dealias
  protected lazy val typeTree     = TypeTree.of[T]
  protected lazy val targetSymbol = typeRepr.typeSymbol

  protected def annotationsWith(s: Symbol): List[Term] =
    targetSymbol.annotations.filter(_.tpe.typeSymbol == s)

  protected def extractAnnotationValue(annotation: Term): Term =
    annotation match
      case Apply(Select(New(_), _), List(arg)) => arg
      case _                                   => report.errorAndAbort(s"Unexpected annotation $annotation")

  protected def annotatedMethods(
      annotationSymbol: Symbol
  ): List[(Symbol, Term)] =
    targetSymbol.methodMembers.map { method =>
      val annots =
        method.annotations.filter(t => annotationSymbol == t.tpe.typeSymbol)
      if annots.length > 1 then
        report.errorAndAbort(
          s"Expected at most one annotation of type dispatchBy, found ${annots.length}"
        )
      if annots.isEmpty then None
      else Option(method, extractAnnotationValue(annots.head))
    }.flatten
