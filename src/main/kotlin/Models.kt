package database

import database.utils.*

internal data class TableRepresentation(
    val name: Identifier,
    val columns: Columns,
    val records: Records,
)

@JvmInline
value class Records internal constructor(val value: List<Record>)

@JvmInline
value class Record internal constructor(
    val properties: List<Property>,
)

@JvmInline
value class Columns internal constructor(val value: List<Column>)

data class Column(
    val name: Identifier,
    val type: ColumnType,
    val primaryKey: Boolean,
)

enum class ColumnType {
    INTEGER,
    STRING,
    BOOLEAN;
}

sealed interface Property {

    @JvmInline
    value class IntProperty(val value: Int) : Property

    @JvmInline
    value class StringProperty(val value: String) : Property

    @JvmInline
    value class BooleanProperty(val value: Boolean) : Property
}

@JvmInline
value class Identifier internal constructor(val value: String) {
    init {
        require(value.isLexemeSyntacticallyValid())
        { "Property is syntactically invalid: $value" }
    }
}
