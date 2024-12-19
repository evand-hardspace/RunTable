package database.file

import kotlin.io.path.Path

internal interface FileWriter {
    fun write(text: String)
    fun append(text: String)
    fun read(): String
    val extension: String
}

internal fun FileWriter(
    path: String,
): FileWriter = object : FileWriter {
    private val file = Path(path).toFile()
    override fun write(text: String) = file.writeText(text)
    override fun append(text: String) = file.appendText(text)
    override fun read(): String = file.readText()
    override val extension: String
        get() = file.extension
}