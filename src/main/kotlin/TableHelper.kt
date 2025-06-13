package database

import database.file.FileWriter
import database.utils.TableValidator
import database.utils.toTable
import database.utils.write
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class TableHelper(
    tableName: Identifier,
    columns: Columns,
    private val fileWriter: FileWriter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1),
) {
    private val validator: TableValidator = TableValidator(fileWriter, columns)
    private var table = TableRepresentation(tableName, columns, Records(emptyList()))

    init {
        if (validator.isRecordsValid().not()) runBlocking { transaction { table } }
        else table = fileWriter.read().toTable()
    }

    suspend fun transaction(action: (currentTable: TableRepresentation) -> TableRepresentation): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                fileWriter.run {
                    table = action(table)
                    table.write()
                }
            }
        }

    fun <T> provide(action: (currentTable: TableRepresentation) -> T): Result<T> =
        runCatching { action(table) }
}
