package com.eyeo.ctu.diffsub2

import java.io.File
import java.io.FileFilter

// saves responses for requests
interface DiffCache {
    // returns `null` if having nothing (no separate `has()`)
    fun get(revisions: Revisions): String?
    fun put(revisions: Revisions, diff: String)
    fun clear()
}

// DiffCache impl that keeps everything in memory
class InMemoryDiffCache : DiffCache {
    private val map = mutableMapOf<Revisions, String>()

    override fun clear() {
        map.clear()
    }

    override fun get(revisions: Revisions): String? {
        return map[revisions]
    }

    override fun put(revisions: Revisions, diff: String) {
        map[revisions] = diff
    }
}

// DiffCache impl that persists in file system
// Warning: requires DiffRequest.toRevision to be not null!
class FileSystemDiffCache(
    // Root directory where cached responses are persisted.
    // It's required to exist!
    private val directory: File,
    private val fileExtension: String = "cached"
) : DiffCache {

    private fun escapeString(s: String) = s.replace("\\W+".toRegex(), "")

    // relative filename for the response file according to the request
    private fun getResponseFileName(request: Revisions): String {
        return "${escapeString(request.from)}_${escapeString(request.to)}.$fileExtension"
    }

    fun getResponseFile(request: Revisions): File {
        return File(directory, getResponseFileName(request))
    }

    override fun get(revisions: Revisions): String? {
        val file = getResponseFile(revisions)
        if (!file.exists()) {
            return null
        }
        return file.readText()
    }

    override fun put(revisions: Revisions, diff: String) {
        val file = getResponseFile(revisions)
        file.writeText(diff)
    }

    private val cachedFilesFilter = FileFilter { file ->
        file != null && file.name.endsWith(".$fileExtension")
    }

    override fun clear() {
        // only cached files (no user files are deleted!)
        val files = directory.listFiles(cachedFilesFilter)
        files?.let {
            it.forEach { file ->
                file.delete()
            }
        }
    }
}