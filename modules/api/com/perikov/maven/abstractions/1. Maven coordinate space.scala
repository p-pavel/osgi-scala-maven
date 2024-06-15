package com.perikov.maven.abstractions

/** Main concepts of Maven coordinates */
type DefaultClassifier = ""

type KnownClassifiers =
  DefaultClassifier | "sources" | "javadoc"

type DefaultPackaging = "jar"
type KnownPackaging =
  DefaultPackaging | "pom" | "war" | "ear" | "rar" | "maven-plugin"

type KnownScopes = "compile" | "runtime" | "provided" | "test" | "system" |
  "import"

type RefinedString[base <: String , regexp <: String] = String
type MavenSeparator = ":"
import compiletime.ops.string.*
type Component = RefinedString[String, "[^" + MavenSeparator + "]+"]
type GroupId  <: Component
type ArtifactId <: Component
type Version  <: Component
type Packaging >: KnownPackaging <: String
type Classifier >: KnownClassifiers <: String

trait IsArtifact[+P <: Packaging, +C <: Classifier,  -T]:
  extension (t: T)
    def groupId: GroupId
    def artifactId: ArtifactId
    def version: Version
    def packaging: P
    def classifier: C

trait Artifacts:
  type Artifact 
  type Jar <: Artifact
  type Pom <: Artifact
  given artifact: IsArtifact[Packaging, Classifier, Artifact]
  given jarArtifact: IsArtifact["jar", Classifier, Jar]
  given pomArtifact: IsArtifact["pom", Classifier, Pom] 
