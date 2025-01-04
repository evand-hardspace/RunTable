package database

import database.api.query
import database.api.record
import database.api.table

suspend fun main() {
    val table = table {
        path = "db/students.rtdb"
        name = "students"
        columns("name") {
            "name".text
            "age".integer
            "is_student".boolean
        }
    }.getOrThrow()

    // delete
    table.delete.all().getOrThrow()

    // insert
    table.insert(record { "Alex" then 33 then false }).getOrThrow()

    // update
    table.update.firstWhere(
        query { "name" eq "Alex" },
        record { "Alex" then 33 then true }
    ).getOrThrow()

    // insert all
    table.insert.all {
        add { "Bob" then 12 then true }
        add { "Alice" then 13 then false }
        add { "Ivan" then 23 then false }
    }.getOrThrow()


    // select
    val result = table.select.allWhere(
        query {
            ("age" lessThan 20) and ("age" moreThan 10) and ("name" contains "Al") or ("is_student" eq true)
        }
    ).getOrNull()

    result?.forEach { println(it) }
}