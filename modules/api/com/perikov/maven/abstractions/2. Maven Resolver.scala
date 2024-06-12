package com.perikov.maven.abstractions
//TODO: TBD
// trait ArtifactInfo:
//   type Manifest

// trait MavenResolver:
//   self =>
//   val mavenSystem: Maven
//   export mavenSystem.*
//   type Set[+A]

//   type Manifest
//   type Stream[_]
//   type F[_]

//   type Cache <: Caching { type F[a] = self.F[a] }
//   val cachingSystem: Cache
//   import cachingSystem.{Cached, Resolver}


//   val obtainPOM: Cached[Artifact, POM]
//   val obtainManifest: Cached[Artifact, Manifest]
//   val obtainVersions: Cached[(GroupId, ArtifactId), Set[Version]]
//   val obtainGroupArtifacts: Cached[GroupId, Set[ArtifactId]]
//   val obtainDependencies: Cached[Artifact, Set[Artifact]]
//   val obtainArtifact: Resolver[Artifact, Stream[Byte]]
