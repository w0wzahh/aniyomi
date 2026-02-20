package eu.kanade.aniyomi.reader.cbz

import android.content.Context
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Extracts image entries from a CBZ/ZIP InputStream into app cache.
 * Returns a sorted list of extracted Files.
 */
object CBZExtractor {
    fun extractImages(context: Context, archiveFile: File, cacheSubDir: String = "cbz_cache"): List<File> {
        val outDir = File(context.cacheDir, cacheSubDir + File.separator + archiveFile.nameWithoutExtension)
        if (!outDir.exists()) outDir.mkdirs()

        ZipInputStream(BufferedInputStream(archiveFile.inputStream())).use { zip ->
            var entry: ZipEntry? = zip.nextEntry
            while (entry != null) {
                try {
                    val name = entry.name
                    if (!entry.isDirectory && isImageName(name)) {
                        val outFile = File(outDir, name)
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out ->
                            zip.copyTo(out)
                        }
                    }
                } catch (_: Exception) {
                }
                entry = zip.nextEntry
            }
        }

        // return sorted list by filename
        return outDir.walkTopDown().filter { it.isFile && isImageName(it.name) }.sortedBy { it.name }.toList()
    }

    private fun isImageName(name: String): Boolean {
        val n = name.lowercase()
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".webp") || n.endsWith(".bmp")
    }
}
