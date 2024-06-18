package com.perikov.cassandra.macros

import scala.annotation.{StaticAnnotation as A}

/** @note
  *   there's a security risk that every method declared in the annotated trait
  *   may be called by it's name
  */
class dispatchByMethodName   extends A
class dispatchBy[Annotation] extends A
class designator(b: Byte)    extends A
