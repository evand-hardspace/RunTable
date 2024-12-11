package database.utils

import database.*
import database.ColumnType.*
import database.Property.*
import java.io.File

internal fun TableRepresentation.writeToFile(file: File) {
    file.writeText("[$name]\n")
    file.appendText(columns.representation())
    file.appendText(records.representation())
}

fun Columns.findIndexOf(column: Column, lazyMessage: () -> Any = {}): Int =
    value.indexOfFirst { it.name == column.name }.also { require(it != -1, lazyMessage) }

fun String.nonEmptyLines() = lines().filter(String::isNotEmpty)

internal fun String.toTable(): TableRepresentation = nonEmptyLines().let { lines ->
    val columns = lines[1].columns
    TableRepresentation(
        name = lines[0].name,
        columns = columns,
        records = lines.drop(2).map { it.record(columns) }.let { Records(it) }
    )
}

infix fun List<Property>.matches(columns: Columns) {
    require(size == columns.value.size)
    { "Provided record properties count are not the same as in table" }
    forEachIndexed { index, prop ->
        require(prop matches columns.value[index].type)
        { "Property type not matches with column type at index $index." }
    }
}

fun List<Property>.validateInputs() {
    forEach { prop ->
        when(prop) {
            is BooleanProperty -> { /* no-op */ }
            is IntProperty -> { /* no-op */ }
            is StringProperty -> {
                require(prop.value.isInputSyntacticallyValid())
                { "Provided string property does not match input rules" }
            }
        }
    }
}

fun Records.checkPrimaryKeyUniqueness(record: Record, columns: Columns) {
    val indexOfPrimaryKey = columns.value.indexOfFirst { it.primaryKey }
    value.map { r ->
        r.properties.filterIndexed { index, _ ->
            columns.value[index].primaryKey
        }.first()
    }.none { it == record.properties[indexOfPrimaryKey] }.also {
        require(it) { "Only unique values are allowed for primary key properties." }
    }
}

private val String.name: String
    get() = inBrackets()

private val String.columns: Columns
    get() = inBrackets().split('|').map {
        val (name, type, primaryKey) = it.split(':')
        require(primaryKey == "P" || primaryKey == "N")
        { "Column does not contain primary key property" }
        Column(name.lexeme, type.asColumnType(), primaryKey.asPrimaryKey())
    }.let { Columns(it) }

fun String.asColumnType(): ColumnType = when (this) {
    "INTEGER" -> INT
    "STRING" -> STRING
    "BOOLEAN" -> BOOLEAN
    else -> error("Column Type is invalid")
}

fun String.asPrimaryKey(): Boolean = when (this) {
    "P" -> true
    "N" -> false
    else -> error("Is not primary key representation")
}

private fun String.record(columns: Columns): Record = Record(
    properties = split('|').mapIndexed { index, record ->
        record.typed(columns.value[index].type)
    }
)

private fun Columns.representation(): String = "[${
    value.map {
        "${it.name.value}:${it.type.representation()}:${it.primaryKey.representation()}"
    }.reduce { acc, s -> "$acc|$s" }
}]\n"

private fun Records.representation(): String = buildString {
    value.forEach { prop: Record ->
        append(prop.properties.map { it.representation() }.reduce { acc, s -> "$acc|$s" })
        append("\n")
    }
}

internal fun Boolean.representation() = if (this) "P" else "N"

internal fun ColumnType.representation(): String = when (this) {
    INT -> "INTEGER"
    STRING -> "STRING"
    BOOLEAN -> "BOOLEAN"
}

internal fun Property.representation() = when (this) {
    is IntProperty -> this.value.toString()
    is StringProperty -> this.value.replace(" ", "?")
    is BooleanProperty -> when (this.value) {
        true -> "TRUE"
        false -> "FALSE"
    }
}

internal fun String.inBrackets(): String =
    substringAfter('[').substringBeforeLast(']')

internal infix fun Property.matches(columnType: ColumnType): Boolean = when (columnType) {
    INT -> this is IntProperty
    STRING -> this is StringProperty
    BOOLEAN -> this is BooleanProperty
}

internal val String.lexeme
    get() = Lexeme(this)
