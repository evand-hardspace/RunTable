package database

import database.file.FileWriter
import database.utils.OpaValidator
import database.utils.toTable
import database.utils.write
import java.io.File

internal class TableHelper(
    tableName: String,
    columns: Columns,
    private val fileWriter: FileWriter,
) {
    private val validator: OpaValidator = OpaValidator(fileWriter, columns)
    private var table = TableRepresentation(tableName, columns, Records(emptyList()))

    init {
        if (validator.isRecordsValid().not()) transaction { table }
        else table = fileWriter.read().toTable()
    }

    fun transaction(action: (currentTable: TableRepresentation) -> TableRepresentation): Unit =
        fileWriter.run {
            table = action(table)
            table.write()
        }

    fun <T> provide(action: (currentTable: TableRepresentation) -> T): T = action(table)
}

