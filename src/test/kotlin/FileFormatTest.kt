import database.*
import database.api.Table
import database.api.integer
import database.api.query
import database.api.text
import database.utils.identifier
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.properties.Delegates.notNull
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FileFormatTest {

    private val fileWriter = InMemoryFileWriter("rtdb")
    private var table: Result<Table> by notNull()

    private suspend fun initTable(
        name: Identifier = "test".identifier,
        columns: List<Column> = listOf(
            Column(
                name = "column_1".identifier,
                ColumnType.STRING,
                primaryKey = true,
            )
        ),
    ) {
        table = Table(
            name = name,
            columns = Columns(columns),
            fileWriter = fileWriter,
        )
    }

    @Test
    fun `GIVEN file is empty, WHEN table is initialized THEN file contains correct metadata`() = runTest {
        // GIVEN
        fileWriter.write("")

        // WHEN
        initTable()

        // THEN
        assertTrue {
            fileWriter.read() == """
                [test]
                [column_1:STRING:P]
                
            """.trimIndent()
        }
    }

    @Test
    fun `GIVEN file has correct metadata, WHEN table is initialized THEN file remains the same`() = runTest {
        // GIVEN
        fileWriter.write(
            """
                [test]
                [column_1:STRING:P]
                
            """.trimIndent()
        )

        // WHEN
        initTable()

        // THEN
        assertTrue {
            fileWriter.read() == """
                [test]
                [column_1:STRING:P]
                
            """.trimIndent()
        }
    }

    @Test
    fun `GIVEN file has invalid syntax metadata, WHEN table is initialized THEN table throws IllegalArgumentException`() = runTest {
        // GIVEN
        fileWriter.write(
            """
                [test]
                [column_1:STRING:P
                
            """.trimIndent()
        )

        // WHEN
        initTable()

        // THEN
        assertFailsWith<IllegalArgumentException> { table.getOrThrow() }
    }

    @Test
    fun `GIVEN file has different metadata, WHEN table is initialized THEN table throws IllegalStateException`() = runTest {
        // GIVEN
        fileWriter.write(
            """
                [test]
                [column_2:STRING:P]
                
            """.trimIndent()
        )

        // WHEN
        initTable(
            columns = listOf(
                Column(
                    name = Identifier("column_1"),
                    ColumnType.STRING,
                    primaryKey = true,
                )
            )
        )

        // THEN
        assertFailsWith<IllegalStateException> { table.getOrThrow() }
    }

    @Test
    fun `select all successful test`() = runTest {
        // GIVEN
        fileWriter.write(
            """
            [test]
            [name:STRING:P|age:INTEGER:N]
            JOHN?SMITH|21
            UNCLE?BOB|44
        """.trimIndent()
        )

        // WHEN
        initTable(
            name = "test".identifier,
            columns = listOf(
                Column(
                    "name".identifier,
                    type = ColumnType.STRING,
                    primaryKey = true,
                ),
                Column(
                    "age".identifier,
                    type = ColumnType.INTEGER,
                    primaryKey = false,
                ),
            )
        )
        val result = table.getOrThrow().select.all().getOrThrow()

        // THEN
        assertTrue {
            result == listOf(
                Record(
                    listOf(
                        text("JOHN SMITH"),
                        integer(21)
                    )
                ),
                Record(
                    listOf(
                        text("UNCLE BOB"),
                        integer(44)
                    )
                ),
            )
        }
    }

    @Test
    fun `select all empty test`() = runTest {
        // GIVEN
        fileWriter.write(
            """
            [test]
            [name:STRING:P|age:INTEGER:N]
        """.trimIndent()
        )

        // WHEN
        initTable(
            name = "test".identifier,
            columns = listOf(
                Column(
                    "name".identifier,
                    type = ColumnType.STRING,
                    primaryKey = true,
                ),
                Column(
                    "age".identifier,
                    type = ColumnType.INTEGER,
                    primaryKey = false,
                ),
            )
        )
        val result = table.getOrThrow().select.all().getOrThrow()

        // THEN
        assertTrue {
            result == emptyList<Record>()
        }
    }

    @Test
    fun `select first where eq test success`() = runTest {
        // GIVEN
        fileWriter.write(
            """
            [test]
            [name:STRING:P|age:INTEGER:N]
            JOHN?SMITH|21
            UNCLE?BOB|44
        """.trimIndent()
        )

        // WHEN
        initTable(
            name = "test".identifier,
            columns = listOf(
                Column(
                    "name".identifier,
                    type = ColumnType.STRING,
                    primaryKey = true,
                ),
                Column(
                    "age".identifier,
                    type = ColumnType.INTEGER,
                    primaryKey = false,
                ),
            )
        )
        val result = table.getOrThrow().select.firstWhere(
            query { "name" eq "JOHN SMITH" }
        ).getOrThrow()

        // THEN
        assertTrue {
            result == Record(
                listOf(
                    text("JOHN SMITH"),
                    integer(21),
                )
            )
        }
    }

    @Test
    fun `select first where eq test failure`() = runTest {
        // GIVEN
        fileWriter.write(
            """
            [test]
            [name:STRING:P|age:INTEGER:N]
            JOHN?SMITH|21
            UNCLE?BOB|44
        """.trimIndent()
        )

        // WHEN
        initTable(
            name = "test".identifier,
            columns = listOf(
                Column(
                    "name".identifier,
                    type = ColumnType.STRING,
                    primaryKey = true,
                ),
                Column(
                    "age".identifier,
                    type = ColumnType.INTEGER,
                    primaryKey = false,
                ),
            )
        )
        val result = table.getOrThrow().select.firstWhere(
            query { "name" eq "ALICE SMITH" }
        ).getOrNull()

        // THEN
        assertNull(result)
    }

    @Test
    fun `select all where eq test success`() = runTest {
        // GIVEN
        fileWriter.write(
            """
            [test]
            [name:STRING:P|age:INTEGER:N]
            JOHN|20
            BOB|21
            ALICE|21
        """.trimIndent()
        )

        // WHEN
        initTable(
            name = "test".identifier,
            columns = listOf(
                Column(
                    "name".identifier,
                    type = ColumnType.STRING,
                    primaryKey = true,
                ),
                Column(
                    "age".identifier,
                    type = ColumnType.INTEGER,
                    primaryKey = false,
                ),
            )
        )
        val result = table.getOrThrow().select.allWhere(
            query { "age" eq 21 }
        ).getOrNull()

        // THEN
        assertTrue {
            result == listOf(
                Record(
                    listOf(
                        text("BOB"),
                        integer(21),
                    )
                ),
                Record(
                    listOf(
                        text("ALICE"),
                        integer(21),
                    )
                ),
            )
        }
    }

    @Test
    fun `select all where eq test failure`() = runTest {
        // GIVEN
        fileWriter.write(
            """
            [test]
            [name:STRING:P|age:INTEGER:N]
            JOHN|20
            BOB|21
            ALICE|21
        """.trimIndent()
        )

        // WHEN
        initTable(
            name = "test".identifier,
            columns = listOf(
                Column(
                    "name".identifier,
                    type = ColumnType.STRING,
                    primaryKey = true,
                ),
                Column(
                    "age".identifier,
                    type = ColumnType.INTEGER,
                    primaryKey = false,
                ),
            )
        )
        val result = table.getOrThrow().select.allWhere(
            query { "age" eq -1 }
        ).getOrThrow()

        // THEN
        assertTrue { result.isEmpty() }
    }
}