import database.file.FileWriter

internal class InMemoryFileWriter(
    override val extension: String,
) : FileWriter {

    private val content = StringBuilder()

    override fun write(text: String) {
        content.clear()
        content.append(text)
    }

    override fun append(text: String) {
        content.append(text)
    }

    override fun read(): String = content.toString()
}