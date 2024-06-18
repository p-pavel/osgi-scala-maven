package com.perikov.cassandra.macros


@FunctionalInterface
trait Writer[T] extends (T => Unit)
