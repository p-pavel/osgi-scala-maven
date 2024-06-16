package com.perikov.maven.abstractions

import cats.effect.*
import cats.effect.implicits.*
import cats.*
import cats.implicits.*
import impl.{naiveArtifactOperations, *}

object Run extends IOApp.Simple:
  def run: IO[Unit] =
    withClient[IO] {
      // extension (s: String) def parseArtifact(using ops: ArtifactOperations[?]) = ops.parseArtifact(s)
      given ops: ArtifactOperations[?] = naiveArtifactOperations
      type Artifact = ops.Artifact
      given api: Impl[IO, Artifact] = Impl[IO, Artifact]()
      someTests[ops.Artifact]

    }
