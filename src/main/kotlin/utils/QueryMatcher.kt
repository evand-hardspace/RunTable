package database.utils

import database.*
import database.Property.IntProperty
import database.Property.StringProperty

interface QueryMatcher {
    fun matches(record: Record, columns: Columns): Boolean
}

internal class OrMatcher(
    private val first: QueryMatcher,
    private val second: QueryMatcher,
) : QueryMatcher {
    override fun matches(record: Record, columns: Columns): Boolean =
        first.matches(record, columns) || second.matches(record, columns)
}

internal class AndMatcher(
    private val first: QueryMatcher,
    private val second: QueryMatcher,
) : QueryMatcher {
    override fun matches(record: Record, columns: Columns): Boolean =
        first.matches(record, columns) && second.matches(record, columns)
}

internal class EqMatcher(
    private val column: Column,
    private val queryProperty: Property,
) : QueryMatcher {
    override fun matches(record: Record, columns: Columns): Boolean {
        val indexOfType = columns.findIndexOf(column) { "No such type in a table" }
        return record.properties[indexOfType] == queryProperty
    }
}

internal class ContainsMatcher(
    private val column: Column,
    private val containedText: String,
) : QueryMatcher {
    override fun matches(record: Record, columns: Columns): Boolean {
        val indexOfType = columns.findIndexOf(column) { "No such type in a table" }
        val prop = record.properties[indexOfType]
        require(prop is StringProperty) { "Property should be STRING" }

        return prop.value.contains(containedText)
    }
}

internal class LessMatcher(
    private val column: Column,
    private val lessValue: Int,
) : QueryMatcher {
    override fun matches(record: Record, columns: Columns): Boolean {
        val indexOfType = columns.findIndexOf(column) { "No such type in a table" }
        val prop = record.properties[indexOfType]
        require(prop is IntProperty) { "Property should be STRING" }

        return prop.value < lessValue
    }
}

internal class MoreMatcher(
    private val column: Column,
    private val moreValue: Int,
) : QueryMatcher {
    override fun matches(record: Record, columns: Columns): Boolean {
        val indexOfType = columns.findIndexOf(column) { "No such type in a table" }
        val prop = record.properties[indexOfType]
        require(prop is IntProperty) { "Property should be STRING" }

        return prop.value > moreValue
    }
}