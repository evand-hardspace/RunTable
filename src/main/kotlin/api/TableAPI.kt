package database.api

import database.*
import database.file.FileWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

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
    suspend fun firstWhere(query: Query): Result<Record>
    suspend fun allWhere(query: Query): Result<List<Record>>
    suspend fun all(): Result<List<Record>>
}

interface DeletableTable {
    suspend fun firstWhere(query: Query): Result<Unit>
    suspend fun allWhere(query: Query): Result<Unit>
    suspend fun all(): Result<Unit>
}

interface InsertableTable {
    suspend operator fun invoke(record: Record): Result<Unit>
    suspend fun all(records: Records): Result<Unit>
}

interface UpdatableTable {
    suspend fun firstWhere(query: Query, record: Record): Result<Unit>
}

internal suspend fun Table(
    name: Identifier,
    columns: Columns,
    fileWriter: FileWriter,
): Result<Table> = withContext(Dispatchers.IO) {
    runCatching {
        object : Table {
            private val tableHelper = TableHelper(name, columns, fileWriter)

            init {
                require(tableHelper.checkPrimaryKeys())
                { "Table should not contain records with duplicate primary keys" }
            }

            override val select: SelectableTable = object : SelectableTable {
                override suspend fun firstWhere(query: Query): Result<Record> =
                    tableHelper.selectFirstWhere(query.column, query.property)

                override suspend fun allWhere(query: Query): Result<List<Record>> =
                    tableHelper.selectAllWhere(query.column, query.property)

                override suspend fun all(): Result<List<Record>> =
                    tableHelper.selectAll()
            }


            override val delete: DeletableTable = object : DeletableTable {
                override suspend fun firstWhere(query: Query): Result<Unit> =
                    tableHelper.deleteFirstWhere(query.column, query.property)

                override suspend fun allWhere(query: Query): Result<Unit> =
                    tableHelper.deleteAllWhere(query.column, query.property)

                override suspend fun all(): Result<Unit> = tableHelper.deleteAll()

            }

            override val insert: InsertableTable = object : InsertableTable {
                override suspend operator fun invoke(record: Record): Result<Unit> = tableHelper.insert(record)

                override suspend fun all(records: Records): Result<Unit> =
                    tableHelper.insertAll(records.value)

            }

            override val update: UpdatableTable = object : UpdatableTable {
                override suspend fun firstWhere(query: Query, record: Record): Result<Unit> =
                    tableHelper.updateFirstWhere(query.column, query.property, record)
            }
        }
    }
}
