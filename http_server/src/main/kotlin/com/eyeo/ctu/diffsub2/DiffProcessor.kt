package com.eyeo.ctu.diffsub2

// API DTO

data class Revisions(
    val from: String,
    var to: String
)

class DiffContext {
    var headRevision: String? = null // is set during the flow (by `AddHeadDiffProcessor`)
}

// calculates diff between the revisions
interface DiffProcessor {
    fun diff(revisions: Revisions, context: DiffContext): String?
}

// DiffProcessor impl that adds HEAD to the Context
class AddHeadDiffProcessor(
    private val gitClient: GitClient,
    private val processor: DiffProcessor
) : DiffProcessor {
    override fun diff(revisions: Revisions, context: DiffContext): String? {
        context.headRevision = gitClient.headRevision()
        return processor.diff(revisions, context)
    }
}

// DiffProcessor impl that does not return diff if the client is already on the HEAD
class AlreadyOnHeadCheckDiffProcessor(
    private val processor: DiffProcessor
) : DiffProcessor {
    override fun diff(revisions: Revisions, context: DiffContext): String? {
        if (context.headRevision == revisions.to /* ".to" is used as "current" */) {
            return null // already on the HEAD, no need to send the diff
        }
        return processor.diff(revisions, context)
    }
}

// DiffProcessor impl that caches the responses
class CachingDiffProcessor(
    private val cache: DiffCache,
    private val processor: DiffProcessor
) : DiffProcessor {
    override fun diff(revisions: Revisions, context: DiffContext): String {
        // ignore racing for the simplicity
        var diff = cache.get(revisions)
        if (diff == null) {
            diff = processor.diff(revisions, context)
            cache.put(revisions, diff!!)
        }
        return diff
    }
}

// DiffProcessor that actually requests Git about the changes.
// It requires `headRevision` to be set!
class GitDiffProcessor(
    private val processor: DiffProcessor,
    private val parser: DiffParser,
    private val converter: Converter
) : DiffProcessor {
    override fun diff(revisions: Revisions, context: DiffContext): String {
        // "revisions.to" contains "current" client revision
        val actualRevisions = Revisions(revisions.from, context.headRevision!!)
        val gitDiff = processor.diff(actualRevisions, context)
        val diff = parser.parse(gitDiff!!)
        val diffResponseBody = converter.convert(diff)
        return String(diffResponseBody)
    }
}