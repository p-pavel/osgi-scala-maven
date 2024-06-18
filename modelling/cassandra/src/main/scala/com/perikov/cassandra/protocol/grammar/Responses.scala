package com.perikov.cassandra.protocol.grammar

import com.perikov.cassandra.protocol.*
import com.perikov.cassandra.macros.dispatchBy

@dispatchBy[opcode]
trait Responses extends Any:
  type Self
  type EventContent
  type ResultContent
  type ErrorContent
  import BasicTypes.*

  /** Indicates an error processing a request.
    *
    * @param code
    * @param message
    * @param errorData
    */
  @opcode(0x00)
  def ERROR(code: int, message: string, errorData: ErrorContent): Self

  /** Indicates that the server is ready to process queries. This message will
    * be sent by the server either after a STARTUP message if no authentication
    * is required (if authentication is required, the server indicates readiness
    * by sending a AUTH_RESPONSE message).
    *
    * The body of a READY message is empty.
    */
  @opcode(0x02)
  def READY: Self

  /** Indicates that the server requires authentication, and which
    * authentication mechanism to use.
    *
    * The authentication is SASL based and thus consists of a number of server
    * challenges (AUTH_CHALLENGE, Section 4.2.7) followed by client responses
    * (AUTH_RESPONSE, Section 4.1.2). The initial exchange is however
    * boostrapped by an initial client response. The details of that exchange
    * (including how many challenge-response pairs are required) are specific to
    * the authenticator in use. The exchange ends when the server sends an
    * AUTH_SUCCESS message or an ERROR message.
    *
    * This message will be sent following a STARTUP message if authentication is
    * required and must be answered by a AUTH_RESPONSE message from the client.
    *
    * The body consists of a single [string] indicating the full class name of
    * the IAuthenticator in use.
    */
  @opcode(0x03)
  def AUTHENTICATE(authClass: string): Self

  /** Indicates which startup options are supported by the server. This message
    * comes as a response to an OPTIONS message.
    *
    * The body of a SUPPORTED message is a [string multimap]. This multimap
    * gives for each of the supported STARTUP options, the list of supported
    * values. It also includes:
    */
  @opcode(0x06)
  def SUPPORTED(options: stringMultimap): Self

  /** The result to a query (QUERY, PREPARE, EXECUTE or BATCH messages).
    *
    * The first element of the body of a RESULT message is an [int] representing
    * the `kind` of result. The rest of the body depends on the kind. The kind
    * can be one of:
    */
  @opcode(0x08)
  def RESULT(rest: ResultContent): Self

  /** An event pushed by the server. A client will only receive events for the
    * types it has REGISTERed to.
    */
  @opcode(0x0c)
  def EVENT(eventType: string, rest: EventContent): Self

  /** A server authentication challenge (see AUTH_RESPONSE (Section 4.1.2) for
    * more details).
    *
    * The body of this message is a single [bytes] token. The details of what
    * this token contains (and when it can be null/empty, if ever) depends on
    * the actual authenticator used.
    *
    * Clients are expected to answer the server challenge with an AUTH_RESPONSE
    * message.
    */
  @opcode(0x0e)
  def AUTH_CHALLENGE(token: bytes): Self

  /** Indicates the success of the authentication phase. See Section 4.2.3 for
    * more details.
    *
    * The body of this message is a single [bytes] token holding final
    * information from the server that the client may require to finish the
    * authentication process. What that token contains and whether it can be
    * null depends on the actual authenticator used.
    */
  @opcode(0x10)
  def AUTH_SUCCESS(token: bytes): Self

end Responses
