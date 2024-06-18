package com.perikov.cassandra.protocol.grammar

import com.perikov.cassandra.protocol.*
import com.perikov.cassandra.macros.dispatchBy

@dispatchBy[opcode]
trait Requests :
  type Self
  import BasicTypes.*

  /** Initialize the connection. The server will respond by either a READY
    * message (in which case the connection is ready for queries) or an
    * AUTHENTICATE message (in which case credentials will need to be provided
    * using AUTH_RESPONSE).
    *
    * This must be the first message of the connection, except for OPTIONS that
    * can be sent before to find out the options supported by the server. Once
    * the connection has been initialized, a client should not send any more
    * STARTUP messages.
    *
    * The body is a [string map] of options. Possible options are:
    * \- "CQL_VERSION": the version of CQL to use. This option is mandatory and
    * currently the only version supported is "3.0.0". Note that this is
    * different from the protocol version.
    * \- "COMPRESSION": the compression algorithm to use for frames (See section
    * 5). This is optional; if not specified no compression will be used.
    */
  @opcode(0x01)
  def STARTUP(options: stringMap): Self

  /**  Answers a server authentication challenge.

  Authentication in the protocol is SASL based. The server sends authentication
  challenges (a bytes token) to which the client answers with this message. Those
  exchanges continue until the server accepts the authentication by sending a
  AUTH_SUCCESS message after a client AUTH_RESPONSE. Note that the exchange
  begins with the client sending an initial AUTH_RESPONSE in response to a
  server AUTHENTICATE request.

  The body of this message is a single [bytes] token. The details of what this
  token contains (and when it can be null/empty, if ever) depends on the actual
  authenticator used.

  The response to a AUTH_RESPONSE is either a follow-up AUTH_CHALLENGE message,
  an AUTH_SUCCESS message or an ERROR message.
    */
  @opcode(0x10)
  def AUTH_RESPONSE(token: bytes): Self

  /** Asks the server to return which STARTUP options are supported. The body of
    * an OPTIONS message should be empty and the server will respond with a
    * SUPPORTED message.
    */
  @opcode(0x05)
  def OPTIONS: Self

  @opcode(0x07)
  def QUERY(query: Nothing, queryParams: Nothing): Self

  /** The server will respond with a RESULT message with a `prepared` kind
    * (0x0004, see Section 4.2.5).
    *
    * @param query
    *   is a [long string] representing the CQL query.
    * @param keyspace
    * @return
    */
  @opcode(0x09)
  def PREPARE(query: Nothing, keyspace: Option[Nothing]): Self

  /** Executes a prepared query. The body of the message must be:
    * `<id><result_metadata_id><query_parameters>`
    *
    * @param id
    *   is the prepared query ID. It's the [short bytes] returned as a response
    *   to a PREPARE message
    * @param resultMetadataId
    *   is the ID of the resultset metadata that was sent along with response to
    *   PREPARE message. If a RESULT/Rows message reports changed resultset
    *   metadata with the Metadata_changed flag, the reported new resultset
    *   metadata must be used in subsequent executions.
    * @param queryParameters
    *   has the exact same definition as in QUERY (see Section 4.1.4)
    * @return
    */
  @opcode(0x0a)
  def EXECUTE(
      id: Nothing,
      resultMetadataId: Nothing,
      queryParameters: Nothing
  ): Self

  /** Register this connection to receive some types of events. The body of the
    * message is a [string list] representing the event types to register for.
    * See section 4.2.6 for the list of valid event types.
    *
    * The response to a REGISTER message will be a READY message.
    *
    * Please note that if a client driver maintains multiple connections to a
    * Cassandra node and/or connections to multiple nodes, it is advised to
    * dedicate a handful of connections to receive events, but to *not* register
    * for events on all connections, as this would only result in receiving
    * multiple times the same event messages, wasting bandwidth.
    *
    * @param params
    * @return
    */
  @opcode(0x0b)
  def REGISTER(params: Nothing): Self

  @opcode(0x0d)
  def BATCH(params: Nothing): Self

end Requests
