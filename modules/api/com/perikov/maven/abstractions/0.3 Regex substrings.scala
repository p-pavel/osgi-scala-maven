package com.perikov.maven.abstractions

import compiletime.ops.string.*
import compiletime.constValue

/** @example
  *   val t3 = "SDfa".refine[FQDNPart] // compiles val
  * @example
  *   t4 = "SDf".refine[FQDN]
  * @example
  *   val t5 = "".refine[FQDN] // does not compile
  */

opaque type RefinedString[Regex <: String] <: String = String

extension (a: String)
  inline def refine[Regex <: String]: RefinedString[Regex] =
    inline if constValue[Matches[a.type, Regex]] then a
    else
      compiletime.error(
        "Cannot refine '" + constValue[
          a.type
        ] + "' as RefinedString[" + constValue[Regex] + "]"
      )

  inline def refineOption[Regex <: String]: Option[RefinedString[Regex]] =
    if compiletime.constValue[Regex].r.matches(a) then Some(a) else None

type FQDNPart = """[a-zA-Z0-9\-_]+"""
type FQDN = FQDNPart + """(\.""" + FQDNPart + ")*"
