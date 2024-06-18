package com.perikov.cassandra.protocol

import scala.annotation.StaticAnnotation as A

class opcode(code: Byte)    extends A
class errorCode(code: Int)  extends A
class resultCode(code: Int) extends A
class consistencyLevel(code: Short) extends A
