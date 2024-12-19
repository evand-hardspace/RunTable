import database.Column
import database.ColumnType
import database.Columns
import database.Identifier
import database.api.Table
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue


class FileFormatTest {

    private val fileWriter = InMemoryFileWriter("rtdb")
    private val sut = Table(
        name = "test",
        columns = Columns(
            listOf(
                Column(
                    name = Identifier("column_1"),
                    ColumnType.STRING,
                    primaryKey = true,
                )
            )
        ),
        fileWriter = fileWriter,
    )

    @Test
    fun test() {
        println(fileWriter.read())
        assertTrue {
            fileWriter.read() == """
                [test]
                [column_1:STRING:P]
                
            """.trimIndent()
        }
    }
}