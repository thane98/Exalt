package compiler

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SourceManager {
    private val searchPaths = mutableListOf<String>()
    private val sources = hashMapOf<String, List<String>>()

    init {
        searchPaths.add(System.getProperty("user.dir"))
    }

    fun addSource(pathString: String): String? {
        val path = findSource(pathString)
        if (!sources.containsKey(path.toString())) {
            val sourceLines = Files.readAllLines(path, Charset.forName("utf-8"))
            sources[path.toString()] = sourceLines
            searchPaths.add(path.toAbsolutePath().parent.toString())
            return path.toString()
        }
        return null
    }

    fun sourceFor(pathString: String): List<String> {
        assert(sources.containsKey(pathString))
        return sources[pathString]!!
    }

    fun removeLastSearchPath() {
        searchPaths.removeAt(searchPaths.size - 1)
    }

    private fun findSource(pathString: String): Path {
        for (path in searchPaths) {
            val target = Paths.get("$path/$pathString")
            if (Files.exists(target) && !Files.isDirectory(target))
                return target.toAbsolutePath()
        }
        throw CompileError(
            "File $pathString is not a valid source file.",
            SourcePosition(null, -1, -1)
        )
    }
}