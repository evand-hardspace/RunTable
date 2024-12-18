package database.utils

import database.Literals

fun String.isRecordSyntacticallyValid(): Boolean =
    matches("^(?:[a-zA-Z0-9?]+(?:\\|[a-zA-Z0-9?]+)*)?\$".toRegex())


fun String.isLexemeSyntacticallyValid(): Boolean =
    matches("^[a-zA-Z0-9_-]+\$".toRegex())

fun String.isIntSyntacticallyValid(): Boolean =
    matches("^[0-9]+\$".toRegex())

fun String.isStringSyntacticallyValid(): Boolean {
    if (all { it == Literals.SPACE_SUBSTITUTION} ) return false
    return matches("^[a-zA-Z0-9?_-]+\$".toRegex())
}

fun String.isInputSyntacticallyValid(): Boolean {
    if (isBlank()) return false
    return matches("^[a-zA-Z0-9 _-]+\$".toRegex())
}

fun String.isBooleanSyntacticallyValid(): Boolean =
    this == Literals.TRUE || this == Literals.FALSE

fun String.asBoolean() = when (this) {
    Literals.TRUE -> true
    Literals.FALSE -> false
    else -> throw IllegalArgumentException("Invalid boolean value: $this")
}
