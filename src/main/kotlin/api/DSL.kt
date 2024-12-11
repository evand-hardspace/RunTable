package database.api

import database.*
import database.utils.lexeme

fun table(
    builder: TableScope.() -> Unit,
): Table = TableScope().apply(builder).toTable()

class TableScope {
    var path: String? = null

    var name: String? = null

    private var columns: Columns? = null

    fun columns(primaryKey: String, block: ColumnScope.() -> Unit) {
        columns = ColumnScope().apply(block).toColumns()
            .value
            .map { column ->
                if (column.name.value == primaryKey) column.copy(primaryKey = true)
                else column
            }.let {
                require(it.count(Column::primaryKey) == 1)
                { "Table columns should contains exactly one primary key" }
                Columns(it)
            }
    }

    internal fun toTable(): Table {
        val p = requireNotNull(path) { "Path should be defined" }
        require(p.isNotBlank()) { { "Path should be not blank" } }

        val n = requireNotNull(name) { "Name should be defined" }
        require(n.isNotBlank()) { { "Name should be not blank" } }

        val c = requireNotNull(columns)
        { "Columns should be defined" }
        return Table(p, n, c)
    }
}

class ColumnScope internal constructor() {
    private val columns = mutableListOf<Column>()

    val String.int: Unit
        get() {
            columns += columnInteger(this)
        }

    val String.text: Unit
        get() {
            columns += columnText(this)
        }

    val String.boolean: Unit
        get() {
            columns += columnBoolean(this)
        }

    internal fun toColumns(): Columns = Columns(columns).also {
        require(it.value.isNotEmpty())
        { "Columns should contain at least one column" }
    }
}

class RecordsScope internal constructor() {
    private val records = mutableListOf<Record>()
    fun add(builder: RecordScope.() -> Unit) {
        records += record(builder)
    }

    fun toRecords(): Records = Records(records)
}

class RecordScope internal constructor() {
    private val properties = mutableListOf<Property>()

    val String.add: Unit
        get() {
            properties += text(this)
        }

    val Int.add: Unit
        get() {
            properties += integer(this)
        }

    val Boolean.add: Unit
        get() {
            properties += boolean(this)
        }

    infix fun String.then(prop: Int): Property {
        val property = text(this)
        val property2 = integer(prop)
        properties += property
        properties += property2
        return property2
    }

    infix fun String.then(prop: String): Property {
        val property = text(this)
        val property2 = text(prop)
        properties += property
        properties += property2
        return property2
    }

    infix fun String.then(prop: Boolean): Property {
        val property = text(this)
        val property2 = boolean(prop)
        properties += property
        properties += property2
        return property2
    }

    infix fun Boolean.then(prop: Int): Property {
        val property = boolean(this)
        val property2 = integer(prop)
        properties += property
        properties += property2
        return property2
    }

    infix fun Boolean.then(prop: String): Property {
        val property = boolean(this)
        val property2 = text(prop)
        properties += property
        properties += property2
        return property2
    }

    infix fun Boolean.then(prop: Boolean): Property {
        val property = boolean(this)
        val property2 = boolean(prop)
        properties += property
        properties += property2
        return property2
    }

    infix fun Int.then(prop: Int): Property {
        val property = integer(this)
        val property2 = integer(prop)
        properties += property
        properties += property2
        return property2
    }

    infix fun Int.then(prop: String): Property {
        val property = integer(this)
        val property2 = text(prop)
        properties += property
        properties += property2
        return property2
    }

    infix fun Int.then(prop: Boolean): Property {
        val property = integer(this)
        val property2 = boolean(prop)
        properties += property
        properties += property2
        return property2
    }

    infix fun Property.then(prop: String): Property {
        val property = text(prop)
        properties += property
        return property
    }

    infix fun Property.then(prop: Int): Property {
        val property = integer(prop)
        properties += property
        return property
    }

    infix fun Property.then(prop: Boolean): Property {
        val property = boolean(prop)
        properties += property
        return property
    }

    internal fun toColumns() = Record(properties)
}

class QueryScope internal constructor() {

    var property: Property? = null
    var column: Column? = null

    infix fun String.eq(value: String) {
        column = columnText(this)
        property = text(value)
    }

    infix fun String.eq(value: Int) {
        column = columnInteger(this)
        property = integer(value)
    }

    infix fun String.eq(value: Boolean) {
        column = columnBoolean(this)
        property = boolean(value)
    }

    fun toQueryEntry(): Query {
        val col = requireNotNull(column)
        { "Column is not specified" }
        val prop = requireNotNull(property) { "Property is not specified" }
        return Query(col, prop)
    }
}

fun query(query: QueryScope.() -> Unit): Query =
    QueryScope().apply(query).toQueryEntry()


fun record(builder: RecordScope.() -> Unit): Record =
    RecordScope().apply(builder).toColumns()

fun records(builder: RecordsScope.() -> Unit): Records =
    RecordsScope().apply(builder).toRecords()

internal fun text(text: String): Property.StringProperty =
    Property.StringProperty(text)

internal fun integer(number: Int): Property.IntProperty =
    Property.IntProperty(number)

internal fun boolean(boolean: Boolean): Property.BooleanProperty =
    Property.BooleanProperty(boolean)

internal fun columnText(name: String, primaryKey: Boolean = false): Column =
    Column(name.lexeme, ColumnType.STRING, primaryKey)

internal fun columnInteger(name: String, primaryKey: Boolean = false): Column =
    Column(name.lexeme, ColumnType.INT, primaryKey)

internal fun columnBoolean(name: String, primaryKey: Boolean = false): Column =
    Column(name.lexeme, ColumnType.BOOLEAN, primaryKey)
