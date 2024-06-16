package com.perikov.maven.abstractions

import org.http4s.ember.client.EmberClientBuilder
import org.http4s.client
import client.Client
import client.middleware.FollowRedirect
import cats.effect.*
import fs2.io.net.Network

def withClient[F[_]](using Async[F], Network[F]) =
  [A] =>
    (f: Client[F] ?=> F[A]) =>
      EmberClientBuilder
        .default[F]
        .build
        .map(cl => FollowRedirect(10)(cl))
        .use(c => f(using c))
