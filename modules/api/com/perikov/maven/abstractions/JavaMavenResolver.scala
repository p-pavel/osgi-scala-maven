package com.perikov.maven.abstractions

trait JavaMavenResolver extends MavenResolver:
  type F[a] = java.util.concurrent.CompletableFuture[a]
  type Stream[a] = a match
    case Byte => java.io.InputStream
