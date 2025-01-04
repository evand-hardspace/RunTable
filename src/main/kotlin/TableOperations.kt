package database

import database.utils.*

internal suspend fun TableHelper.insert(record: Record) = transaction { table ->
    record.properties.validateInputs()
    record.properties matches table.columns
    table.records.checkPrimaryKeyUniqueness(record, table.columns)

    val records = table.records.value.toMutableList()
    records += Record(
        record.properties,
    )
    table.copy(records = Records(records))
}

internal suspend fun TableHelper.insertAll(records: List<Record>) = transaction { table ->
    records.forEach {
        it.properties.validateInputs()
        it.properties matches table.columns
        table.records.checkPrimaryKeyUniqueness(it, table.columns)
    }

    val tableRecords = table.records.value.toMutableList()
    records.forEach { record ->
        tableRecords += Record(
            record.properties,
        )
    }
    table.copy(records = Records(tableRecords))
}

internal suspend fun TableHelper.deleteFirstWhere(matcher: QueryMatcher) = transaction { table ->
    val indexOfRecord = table.records.value
        .indexOfFirst { record -> matcher.matches(record, table.columns) }
    require(indexOfRecord != -1) { "no such value in a table" }

    table.copy(
        records = Records(
            table.records.value.toMutableList().apply { removeAt(indexOfRecord) }
        )
    )
}

internal suspend fun TableHelper.deleteAllWhere(matcher: QueryMatcher) = transaction { table ->
    val indexOfRecord = table.records.value
        .indexOfFirst { record -> matcher.matches(record, table.columns) }
    require(indexOfRecord != -1) { "no such value in a table" }

    table.copy(
        records = Records(
            table.records.value.toMutableList()
                .apply {
                    removeAll { record -> matcher.matches(record, table.columns) }
                }
        )
    )
}

internal suspend fun TableHelper.deleteAll() = transaction { it.copy(records = Records(emptyList())) }

internal fun TableHelper.selectFirstWhere(matcher: QueryMatcher): Result<Record> = provide { table ->
    table.records.value.first { record -> matcher.matches(record, table.columns) }
}

internal fun TableHelper.selectAllWhere(matcher: QueryMatcher): Result<List<Record>> = provide { table ->
    table.records.value.filter { record -> matcher.matches(record, table.columns) }
}

internal fun TableHelper.selectAll(): Result<List<Record>> = provide { it.records.value }

internal suspend fun TableHelper.updateFirstWhere(matcher: QueryMatcher, record: Record) = transaction { table ->
    record.properties.validateInputs()
    record.properties matches table.columns
    val indexOfRecord = table.records.value
        .indexOfFirst { record -> matcher.matches(record, table.columns) }
    require(indexOfRecord != -1) { "no such value in a table" }

    val records = table.records.value.toMutableList()
    records[indexOfRecord] = Record(record.properties)
    table.copy(records = Records(records))
}

internal fun TableHelper.checkPrimaryKeys(): Boolean = provide { table ->
    val primaryKeyIndex = table.columns.value.indexOfFirst { it.primaryKey }
    table.records.value.map {
        it.properties[primaryKeyIndex]
    }.distinct().size == table.records.value.size
}.getOrThrow()
