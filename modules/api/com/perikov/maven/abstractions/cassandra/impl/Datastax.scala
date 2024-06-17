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
  def destroy(s: CassandraSessionDatastax[F]): F[Unit] =
    F.fromCompletionStage(F.delay(s.session.closeAsync())).void

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
      // TODO: How can we avoid the cast here?
      r.get(i, c.runtimeClass.asInstanceOf[Class[T]])

  extension [T](cs: => CompletionStage[T]) def toF: F[T] = F.fromCompletionStage(F.delay(cs))

  extension (p: PreparedStatement)
    override def bind(objs: Any*): BoundStatement =
      p.bind(objs*) // .setConsistencyLevel(datastax.ConsistencyLevel.LOCAL_SERIAL)

  extension (bs: BoundStatement) override def execute: F[ResultSet] = session.executeAsync(bs).toF

  extension (s: String)
    override def executeStatement: F[ResultSet]         = session.executeAsync(s).toF
    override def prepareStatement: F[PreparedStatement] =
      session.prepareAsync(s).toF

class Cache[F[_]: Monad](using session: CassandraSession[F])(
    initialStmt: session.PreparedStatement,
    updateStmt: session.PreparedStatement
):

  def get(key: Seq[Any], compute: F[Seq[Any]]) =
    val insertInitial  = initialStmt.bind((key :+ true)*)
    val insertComputed =
      compute.flatMap { computed =>
        val vars = computed ++ key

        val bound = updateStmt.bind(vars*)
        println("Bound: " + bound)

        bound.execute.void.map(a => println(s"Executed $a"))
      }

    for
      rs      <- insertInitial.execute
      row     <- rs.oneOrError
      inserted = row.get[Boolean](0)
      a        = println("Inserted: " + inserted)
      _       <- if inserted
                 then insertComputed
                 else Monad[F].unit
    yield ()
end Cache

object Cache:
  def apply[F[_]: Monad: Parallel](
      tableName: String,
      inProgressField: String,
      keyFields: Seq[String],
      valFields: Seq[String],
      ttlSeconds: Int
  )(using session: CassandraSession[F]): F[Cache[F]] =
    val lockStmt =
      s""" insert into $tableName 
    |(${keyFields.mkString(", ")}, $inProgressField, ${valFields.mkString(", ")})
    |values
    |(${keyFields.map(_ => "?").mkString(", ")}, ?, ${valFields
          .map(_ => "?")
          .mkString(",")})
    | if not exists using ttl $ttlSeconds""".stripMargin

    val updateStmt =
      s"""update $tableName set $inProgressField = false, ${(valFields)
          .map(f => s"$f = ?")
          .mkString(", ")} where ${keyFields
          .map(f => s"$f = ?")
          .mkString(" and ")} if $inProgressField = true
    """.stripMargin

    println(s"Update: $updateStmt")

    val initial = s"if not exists using ttl $ttlSeconds"
    ((lockStmt).prepareStatement, updateStmt.prepareStatement).parTupled.map(
      new Cache(_, _)
    )

object TstCassandra extends IOApp.Simple:
  import scala.concurrent.duration.*
  def workWithCassandra(using CassandraSession[IO]) =
    val prepared = "SELECT count(*) FROM maven.cacheentry".prepareStatement
    prepared.flatMap(_.bind().execute).flatMap(_.oneOrError).map(_.get[Long](0))

  val run =
    val beelink                      = InetSocketAddress("localhost", 9042)
    val computation: IO[Seq[String]] =
      IO.print("Computing...") >> IO.println("Computed").delayBy(5.nanoseconds) >> IO(Seq("Hello"))
    CassandraSession[IO]("datacenter1", beelink).use { session =>
      given CassandraSession[IO] = session
      val cache                  = Cache[IO]("maven.cacheentry", "inprogress", Seq("id"), Seq("value"), 60)
      cache.flatMap(_.get(Seq(1), computation)) >>
        workWithCassandra(using session).debug("Result:").void
    }
