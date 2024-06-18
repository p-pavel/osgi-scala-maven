package com.perikov.cassandra.protocol.grammar

import com.perikov.cassandra.protocol.*
import com.perikov.cassandra.macros.dispatchByMethodName

/** @todo
  *   descriptions are taken from error messages
  */
@dispatchByMethodName
trait WriteTypes:
  type Self

  /** the write was a non-batched non-counter write. */
  def SIMPLE: Self

  /** the write was a (logged) batch write. If this type is received, it means
    * the batch log has been successfully written (otherwise a "BATCH_LOG" type
    * would have been sent instead)
    */
  def BATCH: Self

  /** the write was an unlogged batch. No batch log write has been attempted.
    */
  def UNLOGGED_BATCH: Self

  /** the write was a counter write (batched or not).
    */
  def COUNTER: Self

  /** the timeout occurred during the write to the batch log when a (logged)
    * batch write was requested
    */
  def BATCH_LOG: Self

  /** the timeout occured during the Compare And Set write/update. */
  def CAS: Self

  /** the timeout occured when a write involves VIEW update and failure to
    * acqiure local view(MV) lock for key within timeout
    */
  def VIEW: Self

  /** the failure occured when cdc_total_space_in_mb is exceeded when doing a
    * write to data tracked by cdc.
    */
  def CDC: Self
end WriteTypes
