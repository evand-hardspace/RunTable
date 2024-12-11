package database

import database.utils.checkPrimaryKeyUniqueness
import database.utils.findIndexOf
import database.utils.matches
import database.utils.validateInputs

internal fun TableHelper.insert(record: Record) = transaction { table ->
    record.properties.validateInputs()
    record.properties matches table.columns
    table.records.checkPrimaryKeyUniqueness(record, table.columns)

    val records = table.records.value.toMutableList()
    records += Record(
        record.properties,
    )
    table.copy(records = Records(records))
}

internal fun TableHelper.insertAll(records: List<Record>) = transaction { table ->
    records.forEach {
        it.properties.validateInputs()
        it.properties matches table.columns
    }

    val tableRecords = table.records.value.toMutableList()
    records.forEach { record ->
        tableRecords += Record(
            record.properties,
        )
    }
    table.copy(records = Records(tableRecords))
}

internal fun TableHelper.deleteFirstWhere(column: Column, value: Property) = transaction { table ->
    val indexOfType = table.columns.findIndexOf(column) { "No such type in a table" }

    val indexOfRecord = table.records.value
        .indexOfFirst { record: Record -> record.properties[indexOfType] == value }
    require(indexOfRecord != -1) { "no such value in a table" }

    table.copy(
        records = Records(
            table.records.value.toMutableList().apply { removeAt(indexOfRecord) }
        )
    )
}

internal fun TableHelper.deleteAllWhere(column: Column, value: Property) = transaction { table ->
    val indexOfType = table.columns.findIndexOf(column) { "No such type in a table" }

    val indexOfRecord = table.records.value
        .indexOfFirst { record: Record -> record.properties[indexOfType] == value }
    require(indexOfRecord != -1) { "no such value in a table" }

    table.copy(
        records = Records(
            table.records.value.toMutableList()
                .apply {
                    removeAll { it.properties[indexOfType] == value }
                }
        )
    )
}

internal fun TableHelper.deleteAll() = transaction { it.copy(records = Records(emptyList())) }

internal fun TableHelper.selectFirstWhere(column: Column, value: Property): Record? = provide { table ->
    val indexOfType = table.columns.findIndexOf(column) { "No such type in a table" }
    table.records.value.firstOrNull { it.properties[indexOfType] == value }
}

internal fun TableHelper.selectAllWhere(column: Column, value: Property): List<Record> = provide { table ->
    val indexOfType = table.columns.findIndexOf(column) { "No such type in a table" }
    table.records.value.filter { it.properties[indexOfType] == value }
}

internal fun TableHelper.selectAll(): List<Record> = provide { it.records.value }

internal fun TableHelper.updateFirstWhere(column: Column, value: Property, record: Record) = transaction { table ->
    record.properties.validateInputs()
    record.properties matches table.columns
    val indexOfType = table.columns.findIndexOf(column) { "No such type in a table" }
    val indexOfRecord = table.records.value
        .indexOfFirst { it.properties[indexOfType] == value }
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
}
