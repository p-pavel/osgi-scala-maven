package com.perikov.maven.abstractions

import cats.effect.*
import scala.jdk.CollectionConverters.*
import aQute.bnd.osgi.{Jar, Analyzer}

extension (s: String) def md5String: String =
  val md = java.security.MessageDigest.getInstance("MD5")
  md.update(s.getBytes)
  md.digest().map("%02x".format(_)).mkString

def processJar[Artifact: ArtifactOperations](artifact: Artifact)(jar: Jar) =
  val analyzer            = Analyzer(jar)
  if jar.getBsn() == null
  then
    analyzer.setBundleSymbolicName(artifact.bundleSymbolicName)
    analyzer.setBundleVersion(artifact.version)
    analyzer.setExportPackage(s"*;version=${artifact.version}")
    analyzer.setImportPackage("scala.*;version=2.13.0")
  analyzer
    .calcManifest()
    .getMainAttributes()
    .asScala
    .toSeq
    .map((k, v) => s"$k:\t\t\t$v")
    .mkString("\n")
  analyzer.analyze()
  val mainAttributes      = analyzer.calcManifest().getMainAttributes()
  val exportPackages      = analyzer.getExports().asScala
  val exportPackageString = //TODO: correctly extract package version
    exportPackages.map((k, v) => s"${k};version=${v.get("version")}").mkString("\n")

  mainAttributes
    .entrySet()
    .asScala
    .toSeq
    .map(e => s"${e.getKey()}:\t\t\t${e.getValue()}")
    .mkString("\n")

  exportPackageString

def someTests[A: ArtifactOperations](using impl.Impl[IO, A]) =
  val artifact = "com.lihaoyi:scalatags_2.13:0.9.1".parseArtifact.toOption.get

    IO.println("asdfasf".md5String)

  // artifact.buildFeatureRepo.flatMap { repo =>
  //   IO.println(s"Feature repo: $repo")
  // }

  // artifact.fetchBndJar.map(processJar(artifact)).flatMap { bsn =>
  //   IO.println(s"BSN: $bsn")
  // }
