package com.perikov.cassandra.protocol.grammar

class TF[A <: Any { type Self }](val unbox: (a: A) ?=> a.Self) extends AnyVal

object BasicTypes:
  type consistency = TF[Consistency]

  /** is a map of endpoint to failure reason codes. This maps the endpoints of
    * the replica nodes that failed when executing the request to a code
    * representing the reason for the failure. The map is encoded starting with
    * an [int] n followed by n pairs of <endpoint><failurecode> where <endpoint>
    * is an [inetaddr] and <failurecode> is a [short].
    */
  type reasonMap = Map[Any, Any]
  type int              = Int
  type short            = Short
  type string           = String

  type writeType =  TF[WriteTypes]
  type byte      = Byte

  type stringList     = Seq[String]
  type shortBytes     = IArray[Byte]
  type stringMultimap = Map[String, Seq[String]]
  type stringMap      = Map[String, String]
  type bytes          = Array[Byte]
  type inet           = String

end BasicTypes

trait BasicTypeReader:
  import BasicTypes.*
  given readString: string
end BasicTypeReader
