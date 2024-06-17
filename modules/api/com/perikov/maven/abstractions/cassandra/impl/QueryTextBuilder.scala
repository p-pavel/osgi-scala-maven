package com.perikov.maven.abstractions.cassandra.impl

type FieldDescription = (String, String)

class QueryTextBuilder(
    keyspace: String,
    tableName: String,
    partitionKeyFields: Seq[FieldDescription],
    clusteringKeyFields: Seq[FieldDescription],
    valueFields: Seq[FieldDescription]
):
  lazy val additionalFieds: Seq[FieldDescription] = Seq.empty
  lazy val keyFields                              = partitionKeyFields ++ clusteringKeyFields
  lazy val keyFieldNames                          = keyFields.map(_._1)
  lazy val valueFieldNames                        = valueFields.map(_._1)
  lazy val allFields                              = keyFields ++ valueFields ++ additionalFieds
  lazy val replicationDescription                 = "{'class': 'SimpleStrategy', 'replication_factor': 1}"
  lazy val createKeyspace                         =
    s"CREATE KEYSPACE IF NOT EXISTS $keyspace WITH REPLICATION = $replicationDescription"
  lazy val fullTableName                          = s"$keyspace.$tableName"
  lazy val partitionDefinition                    =
    partitionKeyFields.map { case (name, _) => s"$name" }.mkString("(", ",", ")")
  lazy val clusteringDefinition                   =
    clusteringKeyFields.map { case (name, _) => s"$name" }.mkString(", ")
  lazy val primaryKeyDefinition                   =
    "(" + Seq(partitionDefinition, clusteringDefinition).mkString(", ") + ")"
  lazy val fieldDefinitions                       = allFields.map { case (name, tpe) => s"$name $tpe" }.mkString(", ")

  lazy val createTable =
    s"CREATE TABLE IF NOT EXISTS $fullTableName ($fieldDefinitions, PRIMARY KEY $primaryKeyDefinition"

end QueryTextBuilder
class CacheQueryTextBuilder(
    keyspace: String,
    tableName: String,
    partitionKeyFields: Seq[FieldDescription],
    clusteringKeyFields: Seq[FieldDescription],
    valueFields: Seq[FieldDescription],
    inProgressField: String
) extends QueryTextBuilder(
      keyspace,
      tableName,
      partitionKeyFields,
      clusteringKeyFields,
      valueFields
    ):

  override lazy val additionalFieds: Seq[FieldDescription] = Seq("inProgress" -> "boolean")

  def lockCacheStatement(ttlSeconds: Int) =
    s"INSERT INTO $fullTableName (${keyFieldNames.mkString(", ")}, $inProgressField) " +
      s"VALUES (${keyFieldNames.map(_ => "?").mkString(", ")}, true) IF NOT EXISTS USING TTL $ttlSeconds"

  lazy val valueUpdatePairs = valueFieldNames.map(f => s"$f = ?")

  def unlockCacheStatement =
    val updateVals = (s"$inProgressField = false" +: valueUpdatePairs).mkString(", ")
    val wherePart  = keyFieldNames.map(f => s"$f = ?").mkString(" AND ")

    s"UPDATE $fullTableName SET $updateVals WHERE $wherePart IF ${inProgressField} = true"

  end unlockCacheStatement

end CacheQueryTextBuilder
