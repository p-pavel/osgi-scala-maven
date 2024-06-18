package com.perikov.cassandra.macros

import scala.annotation.experimental
import scala.quoted.*

/** Conceptually this code is intended as an accessor to the information
  * represented as `trait`. The trait gives us finite set of methods (not
  * implemented or marked with annotation etc). each method gives us cartesian
  * product of its arument.
  * 
  * So we have a disjoint union of product types.
  *
  * We can actually consider method as a record with named fields lying on the
  * stack.
  *
  * The reason for the messy code is some messiveness of the underlying
  * `quotes.reflect` API and the fact that this code has evolved as a series of
  * experiments.
  *
  * @todo
  *   This code needs heavy refactoring.
  * 
  * @todo multiple parameter lists handling is not yet implemented
  *
  * @note
  *   the only reason for `@experimental` annotatation is the usage of
  *   `Symbol.newClass` which is not yet stabilized. I can probably hack around
  *   this but let's hope it will be stabilized soon.
  * 
  * @note public methods supposed to return values not dependent on the 
  * `quotes.reflect` module in use, such as `Expr[T]`
  */
@experimental
class DerivingUtils[T: Type](derivedClassName: String)(using Quotes) extends TraitUtils[T]:
  import quotes.reflect.*


  private def implementationParents: List[TypeTree] =
    List(TypeTree.of[Object], typeTree)

  private def candidatesForOverride =
    typeRepr.typeSymbol.methodMembers
      .map(s => typeRepr.select(s).termSymbol.tree)
      .collect {
        case d: DefDef if d.symbol.flags.is(Flags.Deferred) =>
          if d.paramss.size > 1 then
            report.errorAndAbort(
              s"Only one parameter list is supported for method ${d.name}. Got ${d.paramss.size}"
            )
          d
      }

  private lazy val generatedClassSymbol: Symbol =
    Symbol.newClass(
      Symbol.spliceOwner,
      derivedClassName,
      implementationParents.map(_.tpe),
      cls => candidatesForOverride.map(overridenMethodSymbol(cls)),
      None
    )

  private def overridenMethodSymbol(cls: Symbol)(
      methodToOverride: DefDef
  ): Symbol =
    def unabstractTypeTree(tpt: TypeTree) =
      val sym = tpt.symbol
      if sym.isAbstractType then typeRepr.select(sym)
      else tpt.tpe

    val ret   = unabstractTypeTree(methodToOverride.returnTpt).dealias
    val mtype =
      if methodToOverride.paramss.isEmpty then ByNameType(ret)
      else
        val params = methodToOverride.paramss.map(_.params).flatten
        MethodType(params.map(_.name))(
          m =>
            params.collect { case ValDef(name, tpt, _) =>
              unabstractTypeTree(tpt)
            },
          _ => ret
        )
    Symbol.newMethod(
      cls,
      methodToOverride.name,
      mtype,
      Flags.Override,
      Symbol.noSymbol
    )

  private def overridenPrinterMethod(sym: Symbol): DefDef =
    DefDef(
      sym,
      args => {
        val methName = Expr(sym.name)
        val expr     =
          if args.isEmpty then '{ s"${$methName}" }
          else
            val argNames = Expr.ofList(args.head.map(_.asExpr))
            '{ s"${$methName}(${$argNames.mkString(", ")})" }
        Some(expr.asTerm)
      }
    )


  private def overridenSerializerMethod(sym: Symbol): DefDef =
    val annotations = sym.getAnnotation(TypeRepr.of[designator].typeSymbol)
    report.info(s"Annotations: ${annotations}")
    DefDef(
      sym,
      args =>
        val argsSerialisation = 
          args.flatten.map { arg => 
            val argType = arg.symbol.info
            val neededWriterType = TypeRepr.of[Writer].appliedTo(List(argType))

            val writer = Implicits.search(neededWriterType) match
              case res: ImplicitSearchSuccess => res.tree

              case failure: ImplicitSearchFailure =>
                report.errorAndAbort(
                  s"Cannot find an implicit Writer instance for type ${argType.show(
                      using Printer.TypeReprAnsiCode
                    )}. (Needed for argument ${arg.symbol.name} of method ${sym.name}: ${failure.explanation}"
                )

            val argTerm = arg.asInstanceOf[Term] 

            writer
            .select(
              TypeRepr
                .of[Function1[?, Unit]]
                .typeSymbol
                .methodMember("apply")
                .head
            )
            .appliedTo(argTerm)
          } 
        

        Some(Block(argsSerialisation,'{()}.asTerm))
    )

  def generatePrinterImplementation: Expr[T] =
    generateClassInstantiation(overridenPrinterMethod)

  def generateSerializerImplementation: Expr[T] =
    generateClassInstantiation(overridenSerializerMethod)

  private def generateClassInstantiation(
      methodDefs: Symbol => DefDef
  ): Expr[T] =
    val classSymbol     = generatedClassSymbol
    val printerClassDef =
      ClassDef(
        classSymbol,
        implementationParents,
        generatedClassSymbol.declaredMethods.map(methodDefs)
      )

    val classInstantiation = Typed(
      Apply(
        Select(
          New(TypeIdent(classSymbol)),
          classSymbol.primaryConstructor
        ),
        Nil
      ),
      TypeTree.of[T]
    )

    val block = Block(List(printerClassDef), classInstantiation)

    block.asExprOf[T]
  end generateClassInstantiation
end DerivingUtils
