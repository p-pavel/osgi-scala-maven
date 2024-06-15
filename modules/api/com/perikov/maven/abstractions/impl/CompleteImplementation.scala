package com.perikov.maven.abstractions.impl

import fs2.*
import cats.effect.*
import cats.implicits.*
import cats.effect.implicits.*
import org.http4s
import http4s.client.Client as HttpClient
import com.perikov.maven.abstractions.*

case class FeatureRepo[A](artifact: A, dependencies: Set[A])

class Impl[F[_]:Async: MonadCancelThrow, A](using httpClient: HttpClient[F], ops: ArtifactOperations[A]):
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
      def parseDependency(nodes: NodeSeq): Stream[Pure, Artifact] =
        Stream
          .emits(nodes.map(n =>
            val scope = (n \ "scope").text 
            if scope == "test" then Stream.empty //TODO: handle other scopes
            else
              Stream.emit(
                ops.fromComponents(
                  (n \ "groupId").text,
                  (n \ "artifactId").text,
                  (n \ "version").text
                )
              )
          ))
          .flatten

      Stream
        .eval(a.pom.fetchData.through(io.toInputStream).map(XML.load).compile.lastOrError)
        .flatMap(xml =>
          parseDependency(
            xml \\ "project" \ "dependencies" \ "dependency"
          )
        ).fold(Set.empty[Artifact])(_ + _).compile.lastOrError

    def buildFeatureRepo: F[FeatureRepo[Artifact]] =

      dependencies.map(FeatureRepo(a, _))
