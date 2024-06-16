package com.perikov.maven.abstractions.cassandra.impl

import com.datastax.oss.driver.api
import api.core as datastax
import datastax.cql
import datastax.CqlSession
import com.perikov.maven.abstractions.cassandra.CassandraSession
import cats.effect.*
import cats.*
import cats.implicits.*
import java.util.concurrent.CompletionStage
import scala.jdk.CollectionConverters.*
import java.net.InetSocketAddress
import aQute.bnd.result.Result
import scala.reflect.ClassTag

def CassandraSession[F[_]: Async](
    datacenter: String,
    contactPoints: InetSocketAddress*
): Resource[F, CassandraSession[F]] =
  val F                                       = summon[Async[F]]
  val asyncBuild: CompletionStage[CqlSession] = api.core.CqlSession
    .builder()
    .withLocalDatacenter(datacenter)
    .addContactPoints(contactPoints.asJava)
    .buildAsync()

  val create                                           = F.fromCompletionStage(F.delay(asyncBuild)).map(CassandraSessionDatastax(_))
  def destroy(s: CassandraSessionDatastax[F]): F[Unit] = F.interruptible(s.session.close())

  Resource.make(create)(destroy)
private class CassandraSessionDatastax[F[_]](val session: CqlSession)(using F: Async[F])
    extends CassandraSession[F]:
  override def toString(): String = s"CassandraSessionDatastax($session)"
  override type PreparedStatement = cql.PreparedStatement
  override type Row               = cql.Row
  override type ResultSet         = cql.AsyncResultSet
  override type BoundStatement    = cql.BoundStatement

  extension (rs: ResultSet) override def oneOrError: F[Row] = F.delay(rs.one)

  extension (r: Row)
    override def get[T](using c: ClassTag[T])(i: Int): T =
      //TODO: How can we avoid the cast here?
      r.get(i, c.runtimeClass.asInstanceOf[Class[T]])

  extension [T](cs: => CompletionStage[T]) def toF: F[T] = F.fromCompletionStage(F.delay(cs))

  extension (p: PreparedStatement)
    override def bind(objs: Any*): BoundStatement =
      p.bind(objs*)

  extension (bs: BoundStatement) override def execute: F[ResultSet] = session.executeAsync(bs).toF

  extension (s: String)
    override def executeStatement: F[ResultSet]         = session.executeAsync(s).toF
    override def prepareStatement: F[PreparedStatement] = session.prepareAsync(s).toF

object TstCassandra extends IOApp.Simple:
  def workWithCassandra(using CassandraSession[IO]) =
    val prepared = "SELECT count(*) FROM maven.cacheentry".prepareStatement
    prepared.flatMap(_.bind().execute).flatMap(_.oneOrError).map(_.get[Long](0))

  val run =
    val beelink = InetSocketAddress("beelink", 9042)
    CassandraSession[IO]("datacenter1", beelink).use { session =>
      workWithCassandra(using session).debug("Result:").void
    }
