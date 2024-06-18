package com.perikov.cassandra.macros

import scala.annotation.experimental
import scala.quoted.*


/** Tries to derive a printer implementation for trait T Can be used for
  * tagless-final style traits.
  * {{{
  * trait Abc[T]:
  *   def f1(g: Int, a: String): T
  *
  * derivePrinting[Abc[String]]
  * }}}
  * Will produce an instance of class inherited from `Abc[String]` with `f1`
  * implemented as
  * {{{
  * class Printer extends Abc[String]:
  *   override def f1(g: Int, a: String): String = s"f1($g, $a)"
  * }}}
  *
  * abstract type members are also supported
  *
  * @note
  *   the type parameter `T` should be a class type and not a refined type. Type
  *   aliases are followed though
  *
  * @tparam T
  *   the trait to derive a printer for
  * @return
  *   The implementation of `T`
  */
@experimental
transparent inline def derivePrinting[T]: T = ${ derivePrintingImpl[T](false) }


@experimental
def derivePrintingImpl[T: Type](debug: Boolean)(using q: Quotes) =
  val utils = DerivingUtils[T]("Printer")
  utils.generatePrinterImplementation



