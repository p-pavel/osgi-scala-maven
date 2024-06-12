package com.perikov.maven.abstractions
  type DefaultPackaging = "jar"
  type KnownPackaging = 
    DefaultPackaging 
      | "pom"
      | "war" 
      | "ear" 
      | "rar" 
      | "maven-plugin" 