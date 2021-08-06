package com.eyeo.ctu.diffsub2

import java.io.File
import java.io.FileFilter

// saves responses for requests
interface DiffCache {
    // returns `null` if having nothing (no separate `has()`)
    fun get(request: DiffRequest): DiffResponse?
    fun put(request: DiffRequest, response: DiffResponse)
    fun clear()
}

// DiffCache impl that keeps everything in memory
class InMemoryDiffCache : DiffCache {
    private val map = mutableMapOf<DiffRequest, DiffResponse>()

    override fun clear() {
        map.clear()
    }

    override fun get(request: DiffRequest): DiffResponse? {
        return map[request]
    }

    override fun put(request: DiffRequest, response: DiffResponse) {
        map[request] = response
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
    private fun getResponseFileName(request: DiffRequest): String {
        return "${escapeString(request.fromRevision)}_${escapeString(request.toRevision!!)}.$fileExtension"
    }

    fun getResponseFile(request: DiffRequest): File {
        return File(directory, getResponseFileName(request))
    }

    override fun get(request: DiffRequest): DiffResponse? {
        val file = getResponseFile(request)
        if (!file.exists()) {
            return null
        }
        val diffBody = file.readText()
        return DiffResponse(diffBody, request.toRevision!!)
    }

    override fun put(request: DiffRequest, response: DiffResponse) {
        val file = getResponseFile(request)
        file.writeText(response.diffBody)
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