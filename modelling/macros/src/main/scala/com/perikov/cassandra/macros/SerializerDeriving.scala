package com.perikov.cassandra.macros
import scala.annotation.experimental
import scala.quoted.*

extension [T](t: T)
  inline def write[W[A] <: (A => Unit)](using w: W[T]): Unit = w(t)


@experimental
inline def deriveSerializer[T] = ${ deriveSerializerImpl[T] }


@experimental
def deriveSerializerImpl[T: Type](using Quotes): Expr[T] = 
  val util = DerivingUtils[T]("Serializer")
  util.generateSerializerImplementation
