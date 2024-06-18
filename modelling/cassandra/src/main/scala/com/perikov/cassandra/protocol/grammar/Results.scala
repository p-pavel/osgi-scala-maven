package com.perikov.cassandra.protocol.grammar

import com.perikov.cassandra.protocol.*
import com.perikov.cassandra.macros.dispatchBy

/** Defines the possible content of a [[RESULT]] message.
  */
@dispatchBy[resultCode]
trait Results extends Any:
  type Self
  import BasicTypes.*

  /** results carrying no information. */
  @resultCode(0x0001)
  def Void: Self

  //TODO
  /** results to select queries, returning a set of rows. */
  @resultCode(0x0002)
  def Rows: Self

  /** Set_keyspace: the result to a `use` query.
   * @param kesypace the name of the keyspace that has been set.
   */
  @resultCode(0x0003)
  def Set_keyspace(kesypace: string): Self

  /** result to a PREPARE message. */
  @resultCode(0x0004)
  def Prepared: Self

  /** The result to a schema altering query (creation/update/drop of a
  keyspace/table/index). The body (after the kind [int]) is the same
  as the body for a "SCHEMA_CHANGE" event, so 3 strings:
    `<change_type><target><options>`
  Please refer to section 4.2.6 below for the meaning of those fields.

  Note that a query to create or drop an index is considered to be a change
  to the table the index is on. */
  @resultCode(0x0005)
  def Schema_change: Self
end Results
