package com.perikov.cassandra.protocol.grammar
import com.perikov.cassandra.protocol.*
import com.perikov.cassandra.macros.dispatchByMethodName

/** An event pushed by the server. A client will only receive events for the
  * types it has REGISTERed to. The body of an EVENT message will start with a
  * [string] representing the event type. The rest of the message depends on the
  * event type.
  * @note
  *   All EVENT messages have a streamId of -1 (Section 2.3).
  *
  * Please note that "NEW_NODE" and "UP" events are sent based on internal
  * Gossip communication and as such may be sent a short delay before the binary
  * protocol server on the newly up node is fully started. Clients are thus
  * advised to wait a short time before trying to connect to the node (1 second
  * should be enough), otherwise they may experience a connection refusal at
  * first.
  */
@dispatchByMethodName
trait Events extends Any:
  type ChangeData
  type Self
  import BasicTypes.*

  /** events related to change in the cluster topology. Currently, events are
    * sent when new nodes are added to the cluster, and when nodes are removed.
    * @param changeTyped
    *   ("NEW_NODE" or "REMOVED_NODE")
    * @param nodeAddress
    *   the address of the new/removed node.
    */
  def TOPOLOGY_CHANGE(
      changeType: string,
      nodeAddress: inet
  ): Self

  /** events related to change of node status. Currently, up/down events are
    * sent.
    * @param changeType
    *   ("UP" or "DOWN")
    * @param nodeAddress
    *   the address of the concerned node.
    */
  def STATUS_CHANGE(
      changeType: string,
      nodeAddress: inet
  ): Self

  /** events related to schema change. After the event type,
    * @param changeTyped
    *   one of "CREATED", "UPDATED" or "DROPPED".
    * @param changeData
    *   depends on event target [[SchemaChangeData]].
    */
  def SCHEMA_CHANGE(changeType: string, changeData: ChangeData): Self
end Events
