package com.perikov.cassandra.spikes

import cats.effect.*
import com.perikov.cassandra.protocol.grammar.GrammarPrinter
import scala.annotation.experimental

object Samples:
  val headerBytes = Array[Byte](
    0x05,              // version
    0x0,               // flags
    0x0, 0x0,          // stream
    0x5,               // opcode
    0x0, 0x0, 0x0, 0x0 // length
  )
  def headerByteBuffer() = java.nio.ByteBuffer.wrap(headerBytes)
end Samples

@experimental
object RunCassandra extends IOApp.Simple:
  import com.comcast.ip4s.*
  import fs2.io.net.Network
  import fs2.Chunk
  import java.nio.ByteBuffer
  import Samples.*


  val serverAddr     = SocketAddress(host"beelink", port"9042")
  val client         = Network[IO].client(serverAddr)
  val buf            = headerByteBuffer()
  println((buf.position, buf.capacity(), buf.limit()))


  import fs2.io.net.Socket
  import cats.data.OptionT
  import cats.implicits.*

  def processAnswer(sock: Socket[IO]) = 
    val parseRes = for 
      chunk <- OptionT(sock.read(9))
      headerBuf = chunk.toByteBuffer
      bodyLen = headerBuf.getInt(5)
      bodyChunk <- OptionT(sock.read(bodyLen))
      bodyBuf = bodyChunk.toByteBuffer
      byteArr = new Array[Byte](headerBuf.limit() + bodyBuf.limit())
      _ <- OptionT(IO(headerBuf.get(byteArr, 0, headerBuf.limit()).some))
      _ <- OptionT(IO(bodyBuf.get(byteArr, headerBuf.limit(), bodyBuf.limit()).some))
      msgBuf = ByteBuffer.wrap(byteArr)
      res <- GrammarPrinter.parse(using msgBuf).pure[OptionT[IO,*]]
      
    yield res
    parseRes.value.flatMap(IO.println)

  def run =
    client.use { socket =>
      socket.write(Chunk.byteBuffer(buf)) *> processAnswer(socket)
    }
