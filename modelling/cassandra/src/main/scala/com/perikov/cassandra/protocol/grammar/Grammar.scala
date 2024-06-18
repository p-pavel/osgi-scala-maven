package com.perikov.cassandra.protocol.grammar
import com.perikov.cassandra.macros.*
import java.nio.ByteBuffer
import scala.annotation.experimental

trait Grammar extends Any:
  self =>

  import BasicTypes.*
  type Self

  val results: Results
  val errors: Errors
  val changeData: SchemaChangeData

  val events: Events {
    type ChangeData = changeData.Self
  }

  val responses: Responses {
    type Self          = self.Self
    type ErrorContent  = errors.Self
    type EventContent  = events.Self
    type ResultContent = results.Self
  }

  opaque type Header = byte

    
  given (using b: ByteBuffer): byte   = b.get()
  given (using b: ByteBuffer): short  = b.getShort()
  given (using b: ByteBuffer): int    = b.getInt()
  given (using b: ByteBuffer): string =
    val len   = b.getShort()
    val bytes = new Array[Byte](len)
    b.get(bytes)
    String(bytes, "UTF-8")

  given eventContent: events.Self   = ???
  given errorContent: errors.Self   = ???
  given resultContent: results.Self = ???
  given strList(using b: ByteBuffer): BasicTypes.stringList =
    val len = summon[short]
    Array.fill(len)(summon[string]).toSeq
    
  given (using b: ByteBuffer):BasicTypes.stringMultimap   = 
    val kvCount = summon[short]
    Array.fill(kvCount)(summon[BasicTypes.string] -> summon[BasicTypes.stringList]).toMap






  given (using b: ByteBuffer): bytes =
    val len = b.getInt()
    if len >= 0 then
      val bytes = new Array[Byte](len)
      b.get(bytes)

      bytes
    else null

  private def header(
      version: byte,
      flags: byte,
      stream: short,
      opcode: byte,
      length: int
  ): Header =
    require((version & 0xFF)==  0x85, s"Invalid protocol version: $version")
    opcode

  def message(header: Header)(using rest: ByteBuffer): Self =
    header.dispatchTo(responses)

  def parse(using b: ByteBuffer): Self =
    val hdr = callWithGivens(header)
    message(hdr)

end Grammar

@experimental
object GrammarPrinter extends Grammar:
  type Self = String
  trait SchemaPrinter   extends SchemaChangeData { type Self = String }
  trait ErrorPrinter    extends Errors           { type Self = String }
  trait EventsPrinter   extends Events           {
    type Self = String; type ChangeData = String
  }
  trait ResponsePrinter extends Responses        {
    type Self          = String; type ErrorContent = String; type EventContent = String;
    type ResultContent = String
  }
  trait ResultPrinter   extends Results          { type Self = String }
  val changeData: SchemaPrinter  = derivePrinting
  val errors: ErrorPrinter       = derivePrinting
  val events: EventsPrinter      = derivePrinting
  val results: ResultPrinter     = derivePrinting
  val responses: ResponsePrinter = derivePrinting
end GrammarPrinter
