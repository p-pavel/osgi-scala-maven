package com.perikov.cassandra.protocol.grammar

import com.perikov.cassandra.macros.dispatchBy
import com.perikov.cassandra.protocol.consistencyLevel
import com.perikov.cassandra.macros.dispatchTo

@dispatchBy[consistencyLevel]
trait Consistency extends Any:
  type Self
  @consistencyLevel(0x0000) def ANY: Self
  @consistencyLevel(0x0001) def ONE: Self
  @consistencyLevel(0x0002) def TWO: Self
  @consistencyLevel(0x0003) def THREE: Self
  @consistencyLevel(0x0004) def QUORUM: Self
  @consistencyLevel(0x0005) def ALL: Self
  @consistencyLevel(0x0006) def LOCAL_QUORUM: Self
  @consistencyLevel(0x0007) def EACH_QUORUM: Self
  @consistencyLevel(0x0008) def SERIAL: Self
  @consistencyLevel(0x0009) def LOCAL_SERIAL: Self
  @consistencyLevel(0x000a) def LOCAL_ONE: Self
