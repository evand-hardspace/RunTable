package database

import database.utils.OpaValidator
import database.utils.toTable
import database.utils.writeToFile
import java.io.File

internal class TableHelper(
    path: String,
    tableName: String,
    columns: Columns,
) {
    private val file = File(path)
    private val validator: OpaValidator = OpaValidator(file, columns)
    private var table = TableRepresentation(tableName, columns, Records(emptyList()))

    init {
        if (validator.isRecordsValid().not()) transaction { table }
        else table = file.readText().toTable()
    }

    fun transaction(action: (currentTable: TableRepresentation) -> TableRepresentation) {
        table = action(table)
        table.writeToFile(file)
    }

    fun <T> provide(action: (currentTable: TableRepresentation) -> T): T = action(table)
}

