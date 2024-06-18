package com.perikov.maven.abstractions.impl

import fs2.*
import cats.effect.*
import cats.implicits.*
import org.http4s
import http4s.client.Client as HttpClient
import com.perikov.maven.abstractions.*
import aQute.bnd.osgi.Jar
import java.io.InputStream

case class FeatureRepo[A](artifact: A, dependencies: Set[A])

class Impl[F[_]: Async: MonadCancelThrow, A](using
    httpClient: HttpClient[F],
    ops: ArtifactOperations[A]
):
  type Artifact = A
  extension (a: Artifact)
    def fetchData: Stream[F, Byte] =
      import org.http4s.*
      import org.http4s.implicits.*
      val baseUri     = uri"https://repo.maven.apache.org/maven2/"
      val artifactUri = a.pathComponents.foldLeft(baseUri: Uri)(_ / _)
      Stream
        .resource(httpClient.run(Request(uri = artifactUri)))
        .flatMap(_.body)

    def dependencies: F[Set[Artifact]] =
      import scala.xml.*
      def parseDependency(root: Elem): Set[Artifact] =
        val nodes = root \\ "project" \ "dependencies" \ "dependency"
        nodes.foldLeft(Set.empty)((res, n) =>
          val scope = (n \ "scope").text
          if scope == "test" then res
          else
            res + ops.fromComponents(
              (n \ "groupId").text,
              (n \ "artifactId").text,
              (n \ "version").text
            )
        )

      fetchSomethingUnsafe(XML.load).map(parseDependency)

    private def fetchSomething[A](f: InputStream => F[A]): F[A] =
      a.fetchData.through(io.toInputStream).evalMap(f).compile.lastOrError

    private def fetchSomethingUnsafe[A](f: InputStream => A): F[A] =
      fetchSomething(a => Sync[F].delay(f(a)))

    def fetchBndJar: F[Jar] = fetchSomethingUnsafe(Jar(a.filename, _))

    def buildFeatureRepo: F[FeatureRepo[Artifact]] =
      dependencies.map(FeatureRepo(a, _))
