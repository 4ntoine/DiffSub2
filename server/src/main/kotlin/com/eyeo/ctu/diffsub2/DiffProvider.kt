package com.eyeo.ctu.diffsub2

import java.io.File

data class DiffRequest(
    val fromRevision: String,
    val toRevision: String? = null
)

data class DiffResponse(
    val diffBody: String,
    val headRevision: String // might be needed to improve the performance (caching can be used)
)

// calculates diff between the revisions
interface DiffProvider {
    fun diff(request: DiffRequest): DiffResponse
}

// DiffProvider impl that caches the responses
class CachingDiffProvider(
    private val cache: DiffCache,
    private val provider: DiffProvider
) : DiffProvider {
    override fun diff(request: DiffRequest): DiffResponse {
        // ignore racing for the simplicity
        var response = cache.get(request)
        if (response == null) {
            response = provider.diff(request)
            cache.put(request, response)
        }
        return response
    }
}

// TODO: there could be JGit-based impl for GitClient

// DiffProvider impl that adds `toRevision` = HEAD if it's missing
class ToRevisionDiffProvider(
    private val gitClient: GitClient,
    private val provider: DiffProvider
) : DiffProvider {
    override fun diff(request_: DiffRequest): DiffResponse {
        var request = request_
        if (request.toRevision == null) {
            request = DiffRequest(
                request.fromRevision,
                gitClient.getHeadRevision()
            )
        }
        return provider.diff(request)
    }
}

// DiffProvider that actually requests Git about the changes.
// It requires `fromRevision` and `toRevision` are set!
class GitDiffProvider(
    private val gitClient: GitClient,
    private val parser: DiffParser,
    private val converter: Converter
) : DiffProvider {
    override fun diff(request: DiffRequest): DiffResponse {
        val toRevision = request.toRevision!!
        val gitDiff = gitClient.getDiff(request.fromRevision, toRevision)
        val diff = parser.parse(gitDiff)
        val diffResponseBody = converter.convert(diff)
        return DiffResponse(String(diffResponseBody), toRevision)
    }
}