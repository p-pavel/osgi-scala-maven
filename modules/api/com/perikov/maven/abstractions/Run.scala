package com.perikov.maven.abstractions

import cats.effect.*
import cats.effect.implicits.*
import cats.*
import cats.implicits.*
import impl.{naiveArtifactOperations, *}

object Run extends IOApp.Simple:
  def run: IO[Unit] =
    withClient[IO] {
      extension (s: String) def parseArtifact(using ops: ArtifactOperations[?]) = ops.parseArtifact(s)
      given ops: ArtifactOperations[?]                                          = naiveArtifactOperations
      val api                                                                   = Impl[IO, ops.Artifact]()
      api.buildFeatureRepo("com.lihaoyi:scalatags_2.13:0.9.1".parseArtifact.toOption.get).flatMap { repo =>
        IO.println(s"Feature repo: $repo")
      }

    }
