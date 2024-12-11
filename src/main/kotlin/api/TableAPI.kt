package database.api

import database.*
import database.TableHelper

data class Query internal constructor(
    val column: Column,
    val property: Property,
)

interface Table {
    val select: SelectableTable
    val delete: DeletableTable
    val insert: InsertableTable
    val update: UpdatableTable
}

interface SelectableTable {
    fun firstWhere(query: Query): Record?
    fun allWhere(query: Query): List<Record>
    fun all(): List<Record>
}

interface DeletableTable {
    fun firstWhere(query: Query)
    fun allWhere(query: Query)
    fun all()
}

interface InsertableTable {
    operator fun invoke(record: Record)
    fun all(records: Records)
}

interface UpdatableTable {
    fun firstWhere(query: Query, record: Record)
}

fun Table(
    path: String,
    name: String,
    columns: Columns,
): Table = object : Table {
    private val tableHelper = TableHelper(path, name, columns)

    init {
        require(tableHelper.checkPrimaryKeys())
        { "Table should not contain records with duplicate primary keys" }
    }

    override val select: SelectableTable = object : SelectableTable {
        override fun firstWhere(query: Query): Record? =
            tableHelper.selectFirstWhere(query.column, query.property)

        override fun allWhere(query: Query): List<Record> =
            tableHelper.selectAllWhere(query.column, query.property)

        override fun all(): List<Record> =
            tableHelper.selectAll()
    }


    override val delete: DeletableTable = object : DeletableTable {
        override fun firstWhere(query: Query): Unit =
            tableHelper.deleteFirstWhere(query.column, query.property)

        override fun allWhere(query: Query): Unit =
            tableHelper.deleteAllWhere(query.column, query.property)

        override fun all(): Unit = tableHelper.deleteAll()

    }

    override val insert: InsertableTable = object : InsertableTable {
        override operator fun invoke(record: Record): Unit = tableHelper.insert(record)

        override fun all(records: Records): Unit =
            tableHelper.insertAll(records.value)

    }

    override val update: UpdatableTable = object : UpdatableTable {
        override fun firstWhere(query: Query, record: Record): Unit =
            tableHelper.updateFirstWhere(query.column, query.property, record)
    }
}

