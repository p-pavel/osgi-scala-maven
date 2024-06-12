package com.perikov.maven.abstractions

//TODO: Rename
trait ComponentProduct:
  type Self
  type Components
  def toComponents(t: Self): Components
  def fromComponents(c: Components): Self 

type ComponentProductAux[T, Comp] = ComponentProduct {
  type Self = T; type Components = Comp
}

extension [Comp](components: Comp)
  inline def fromComponents[A](using evidence: ComponentProductAux[A, Comp]) =
    evidence.fromComponents(components)

extension [A](a: A)
  inline def toComponents[Comp](using
      evidence: ComponentProductAux[A, Comp]
  ): Comp =
    evidence.toComponents(a)
