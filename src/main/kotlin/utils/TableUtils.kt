package database.utils

import database.*
import database.ColumnType.*
import database.Property.*
import database.file.FileWriter

context(FileWriter)
internal fun TableRepresentation.write() {
    write("[${name.value}]\n")
    append(columns.representation())
    append(records.representation())
}

fun Columns.findIndexOf(column: Column, lazyMessage: () -> Any = {}): Int =
    value.indexOfFirst { it.name == column.name }.also { require(it != -1, lazyMessage) }

fun String.nonEmptyLines() = lines().filter(String::isNotEmpty)

internal fun String.toTable(): TableRepresentation = nonEmptyLines().let { lines ->
    val columns = lines[1].columns
    TableRepresentation(
        name = lines[0].name.identifier,
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
    get() = inBrackets().split(Literals.SEPARATOR).map {
        val (name, type, primaryKey) = it.split(Literals.TYPE_SEPARATOR)
        require(primaryKey == Literals.PRIMARY_KEY || primaryKey == Literals.NON_PRIMARY_KEY)
        { "Column does not contain primary key property" }
        Column(name.identifier, type.asColumnType(), primaryKey.asPrimaryKey())
    }.let { Columns(it) }

fun String.asColumnType(): ColumnType = when (this) {
    Literals.INTEGER -> INTEGER
    Literals.STRING -> STRING
    Literals.BOOLEAN -> BOOLEAN
    else -> error("Column Type is invalid")
}

fun String.asPrimaryKey(): Boolean = when (this) {
    Literals.PRIMARY_KEY -> true
    Literals.NON_PRIMARY_KEY -> false
    else -> error("Is not primary key representation")
}

private fun String.record(columns: Columns): Record = Record(
    properties = split(Literals.SEPARATOR).mapIndexed { index, record ->
        record.typed(columns.value[index].type)
    }
)

private fun Columns.representation(): String = "[${
    value.map {
        "${it.name.value}${Literals.TYPE_SEPARATOR}${it.type.representation()}${Literals.TYPE_SEPARATOR}${it.primaryKey.representation()}"
    }.reduce { acc, s -> "$acc${Literals.SEPARATOR}$s" }
}]\n"

private fun Records.representation(): String = buildString {
    value.forEach { prop: Record ->
        append(prop.properties.map { it.representation() }.reduce { acc, s -> "$acc${Literals.SEPARATOR}$s" })
        append("\n")
    }
}

internal fun Boolean.representation() = if (this) Literals.PRIMARY_KEY else Literals.NON_PRIMARY_KEY

internal fun ColumnType.representation(): String = when (this) {
    INTEGER -> Literals.INTEGER
    STRING -> Literals.STRING
    BOOLEAN -> Literals.BOOLEAN
}

internal fun Property.representation() = when (this) {
    is IntProperty -> this.value.toString()
    is StringProperty -> this.value.replace(" ", Literals.SPACE_SUBSTITUTION.toString())
    is BooleanProperty -> when (this.value) {
        true -> Literals.TRUE
        false -> Literals.FALSE
    }
}

internal fun String.inBrackets(): String {
    require(first() == '[' && last() == ']') { "Value should be in brackets" }
    return substringAfter('[').substringBeforeLast(']')
}

internal infix fun Property.matches(columnType: ColumnType): Boolean = when (columnType) {
    INTEGER -> this is IntProperty
    STRING -> this is StringProperty
    BOOLEAN -> this is BooleanProperty
}

internal val String.identifier
    get() = Identifier(this)
