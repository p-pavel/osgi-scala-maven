package com.perikov.maven.abstractions

/** Main concepts of Maven coordinates */
trait MavenCoordinates:
  /** @todo make more precise */
  type GroupId <: String

  /** @todo make more precise */
  type ArtifactId <: String

  type Version

  type Classifier >: KnownClassifiers <: String

  type Packaging >: KnownPackaging <: String
  
  type Scope >: KnownScopes <: String



