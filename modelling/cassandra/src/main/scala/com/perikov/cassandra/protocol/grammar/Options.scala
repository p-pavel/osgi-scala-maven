package com.perikov.cassandra.protocol.grammar
import com.perikov.cassandra.protocol.*
import com.perikov.cassandra.macros.dispatchByMethodName

@dispatchByMethodName
trait Options:
  type Self
  import BasicTypes.*

  /** the version of CQL to use. This option is mandatory and currently the only
    * version supported is "3.0.0". Note that this is different from the
    * protocol version.
    *
    * @param version
    *   This option is mandatory and currently the only version supported is
    *   "3.0.0". Note that this is different from the protocol version.
    */
  def CQL_VERSION(version: string): Self

  /** @param compression
    *   the compression algorithm to use for frames (See section 5). This is
    *   optional; if not specified no compression will be used.
    */
  def COMPRESSION(compression: string): Self

  /** @param version
    *   the list of native protocol versions that are supported, encoded as the
    *   version number followed by a slash and the version description. For
    *   example: 3/v3, 4/v4, 5/v5-beta. If a version is in beta, it will have
    *   the word "beta" in its description.
    */
  def PROTOCOL_VERSION(version: string): Self
end Options
