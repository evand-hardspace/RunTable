package database.utils

import database.*
import database.file.FileWriter

internal class OpaValidator(
    private val fileWriter: FileWriter,
    private val columns: Columns,
) {

    /**
     * throws IllegalArgumentException if file extension or format is invalid
     * returns false when file is valid but empty
     * returns true when file extension and format are valid
     */
    private fun isMetadataValid(): Boolean {
        if (fileWriter.extension != FILE_EXTENSION) error("Extension is not .${FILE_EXTENSION}")
        val lines = fileWriter.read().nonEmptyLines()
        if (lines.isEmpty()) return false
        val title = lines.first()
        if (title.matches("\\[[A-Za-z]+]".toRegex()).not()) error("Title format is invalid")

        val fileColumns = lines[1].inBrackets().split(Literals.SEPARATOR)
            .map {
                val (name, type, primaryKey) = it.split(Literals.TYPE_SEPARATOR)
                require(primaryKey == Literals.PRIMARY_KEY || primaryKey == Literals.NON_PRIMARY_KEY)
                { "Column does not contain primary key property" }
                Column(name.identifier, type.asColumnType(), primaryKey.asPrimaryKey())
            }
        if (fileColumns.count { it.primaryKey } != 1) error("Table should contain exactly one primary key")
        if (fileColumns.size != columns.value.size) error("Existing types count are not matching with provided types count")
        val isTypesMatching = fileColumns.zip(columns.value).all { (f, t) -> f == t }
        if (isTypesMatching.not()) error("Existing types are not marching with provided types")
        return true
    }

    /**
     * returns false when metadata is invalid or records are empty
     * returns true when metadata is valid and records format is valid
     * throws exception when metadata or records format are invalid
     */
    fun isRecordsValid(): Boolean {
        if (isMetadataValid().not()) return false
        val lines = fileWriter.read().nonEmptyLines()
        if (lines.size == 2) return false
        val types = lines[1].split(Literals.SEPARATOR)

        val typedColumns = lines[1].inBrackets().split(Literals.SEPARATOR)
            .map {
                val (name, type, primaryKey) = it.split(Literals.TYPE_SEPARATOR)
                require(primaryKey == Literals.PRIMARY_KEY || primaryKey == Literals.NON_PRIMARY_KEY)
                { "Column does not contain primary key property" }
                Column(name.identifier, type.asColumnType(), primaryKey.asPrimaryKey())
            }

        val rawRecords = lines
            .drop(2)
            .onEach { line ->
                require(line.isRecordSyntacticallyValid())
                { "Record syntax is invalid" }
            }.map { line -> line.split(Literals.SEPARATOR) }

        val typedRecords = rawRecords.map { raw ->
            raw.mapIndexed { index, r ->
                val columnType = typedColumns.getOrNull(index)?.type
                requireNotNull(columnType) { "Record size is not matching with types size" }
                r.typed(columnType)
            }
        }

        typedRecords
            .forEach { record ->
                require(record.size == types.size)
                { "Record properties count is not matching with types size" }
                record.forEachIndexed { index, prop ->
                    prop matches typedColumns[index].type
                }
            }
        return true
    }
}

internal fun String.typed(columnType: ColumnType): Property =
    when (columnType) {
        ColumnType.INTEGER -> {
            require(isIntSyntacticallyValid())
            { "Integer property is syntactically invalid: $this" }
            Property.IntProperty(toInt())
        }

        ColumnType.STRING -> {
            require(isStringSyntacticallyValid())
            { "String property is syntactically invalid: $this" }
            Property.StringProperty(replace(Literals.SPACE_SUBSTITUTION.toString(), " "))
        }

        ColumnType.BOOLEAN -> {
            require(isBooleanSyntacticallyValid())
            { "Boolean property is syntactically invalid: $this" }
            Property.BooleanProperty(asBoolean())
        }
    }
