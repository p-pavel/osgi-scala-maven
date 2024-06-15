package com.perikov.maven.abstractions

import scala.util.NotGiven

object hkt:
  trait Reflexive[K <: AnyKind, <=[A <: K, B <: K]]:
    def refl[A <: K]: A <= A

  trait Transitive[K <: AnyKind, <=[A <: K, B <: K]]:
    extension [A <: K, B <: K, C <: K](ab: A <= B) def andThen(bc: B <= C): A <= C

  trait Symmetric[K <: AnyKind, <=[A <: K, B <: K]]:
    extension [A <: K, B <: K](ab: A <= B) def flip: B <= A

  type Preorder[K <: AnyKind, <=[A <: K, B <: K]] = Reflexive[K, <=] & Transitive[K, <=]

  type Equivalence[K <: AnyKind, <=[A <: K, B <: K]] = Preorder[K, <=] & Symmetric[K, <=]

  given [K <: AnyKind, A <: K, R[_ <: K, _ <: K]](using
      ev: Reflexive[K, R],
      default: NotGiven[R[A, A]]
  ): R[A, A] = ev.refl
