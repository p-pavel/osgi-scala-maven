package com.perikov.maven.abstractions.cassandra

import scala.reflect.ClassTag


trait CassandraSession[F[_]]:
  type PreparedStatement
  type BoundStatement
  type Row
  type ResultSet

  extension (rs: ResultSet) def oneOrError: F[Row]

  extension (row: Row) def get[T](using ClassTag[T])(i: Int): T

  extension (p: PreparedStatement) def bind(t: Any*): BoundStatement
  extension (bs: BoundStatement) def execute: F[ResultSet]

  extension (stmt: String) def executeStatement: F[ResultSet]
  extension (stmt: String) def prepareStatement: F[PreparedStatement]

transparent inline def cassandraSession[F[_]](using cass: CassandraSession[F]): cass.type = cass
