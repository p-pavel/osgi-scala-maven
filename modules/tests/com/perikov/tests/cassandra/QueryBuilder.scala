package com.perikov.tests.cassandra

import com.perikov.maven.abstractions.cassandra.impl.*
class QueryBuilder extends munit.FunSuite:
  val builder = CacheQueryTextBuilder(
    "maven",
    "cachentry",
    partitionKeyFields = Seq("id" -> "text"),
    clusteringKeyFields = Seq("time" -> "timestamp"),
    valueFields = Seq("value" -> "text"),
    inProgressField = "inProgress"
  )

  test("create keyspace") {
    assertEquals(
      builder.createKeyspace,
      "CREATE KEYSPACE IF NOT EXISTS maven WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1}"
    )
  }

  test("create table") {
    assertEquals(
      builder.createTable,
      "CREATE TABLE IF NOT EXISTS maven.cachentry (id text, time timestamp, value text, inProgress boolean, PRIMARY KEY ((id), time)"
    )
  }

  test("lock cache") {
    assertEquals(
      builder.lockCacheStatement(10),
      "INSERT INTO maven.cachentry (id, time, inProgress) VALUES (?, ?, true) IF NOT EXISTS USING TTL 10"
    )
  }

  test("fill cache") {
    assertEquals(
      builder.unlockCacheStatement,
      "UPDATE maven.cachentry SET inProgress = false, value = ? WHERE id = ? AND time = ? IF inProgress = true"
    )
  }
end QueryBuilder
