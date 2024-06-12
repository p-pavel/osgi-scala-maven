package com.perikov.maven.abstractions

opaque type Unknown = AnyRef
val Unknown: Unknown = null

type Optional[T] = T | Unknown
