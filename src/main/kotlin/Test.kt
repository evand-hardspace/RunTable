package database

import database.api.query
import database.api.records
import database.api.table
import kotlin.random.Random


fun main() {
    val table = table {
        path = "db/test.rtdb"
        name = "test"
        columns("name") {
            "name".text
            "age".int
            "is_student".boolean
        }
    }

    table.delete.all()
    table.insert.all(
        records {
            repeat(1000) { i ->
                add { "Test$i" then i then Random.nextBoolean() }
            }
        },
    )

    val result = table.select.allWhere(
        query { "is_student" eq true }
    ).count()

    println(result)
//    result.forEach { println(it) }
}
