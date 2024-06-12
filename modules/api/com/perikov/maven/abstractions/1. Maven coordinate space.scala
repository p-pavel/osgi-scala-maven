package com.perikov.maven.abstractions

/** Main concepts of Maven coordinates */
trait MavenCoordinates:
  /** @todo: is this correct? */
  type GroupId <: RefinedString[FQDN]

  type NoSeparator = RefinedString["[^:]+"]

  /** @todo: is this correct? */
  type ArtifactId <: NoSeparator


  type Version

  /** @todo what can we tell about version string? */
  type VersionString <: NoSeparator

  extension (v: Version) def versionString: VersionString
  
  /** We should be able to check string for validity */
  extension (s: String) def mavenVersionOption: Option[Version]

  type Classifier >: KnownClassifiers <: String

  type Packaging >: KnownPackaging <: String
  
  type Scope >: KnownScopes <: String



