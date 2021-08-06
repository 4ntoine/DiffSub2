package com.eyeo.ctu.diffsub2

import org.junit.Test
import java.io.File
import kotlin.test.*

class DiffProcessorTest {

    private class TestDiffProcessor(
        val response: String?
    ) : DiffProcessor {
        var callCounter = 0
        override fun diff(revisions: Revisions, context: DiffContext): String? {
            callCounter++
            return response
        }
    }

    @Test
    fun testAddHeadDiffProcessor() {
        class TestGitClient(
            val diff: String?,
            val head: String
        ) : GitClient {
            var called = false
            override fun diff(revisions: Revisions, context: DiffContext): String? {
                called = true
                return diff
            }

            override fun headRevision(): String {
                called = true
                return head
            }
        }

        val diff = "diff"
        val head = "HEAD"
        val gitClient = TestGitClient(diff, head)
        val diffProcessor = TestDiffProcessor(diff)
        val addHeadDiffProcessor = AddHeadDiffProcessor(gitClient, diffProcessor)
        val context = DiffContext()
        assertNull(context.headRevision)
        addHeadDiffProcessor.diff(Revisions("from", "to"), context)
        assertNotNull(context.headRevision)
        assertTrue(gitClient.called)
        assertTrue(diffProcessor.callCounter > 0)
    }

    @Test
    fun testAlreadyOnHeadCheckDiffProcessor() {
        val response = "response"
        val processor = TestDiffProcessor(response)
        val checkProcessor = AlreadyOnHeadCheckDiffProcessor(processor)
        val head = "HEAD"
        val context = DiffContext()
        context.headRevision = head
        val response1 = checkProcessor.diff(Revisions("from", head), context) // already on HEAD
        assertTrue(processor.callCounter == 0)
        assertNull(response1) // no changes
        val response2 = checkProcessor.diff(Revisions("from", "not_$head"), context) // Not on HEAD
        assertTrue(processor.callCounter > 0)
        assertEquals(response, response2) // some changes from wrapped processor
    }

    @Test
    fun testCachingDiffProcessor() {
        val response = "response"
        val processor = TestDiffProcessor(response)
        val cache = InMemoryDiffCache()
        val cachingProcessor = CachingDiffProcessor(cache, processor)
        val revisions = Revisions("from", "to")
        val context = DiffContext()
        val response1 = cachingProcessor.diff(revisions, context)
        assertEquals(response, response1)
        assertEquals(response, cache.get(revisions))
        assertEquals(1, processor.callCounter)
        val response2 = cachingProcessor.diff(revisions, context)
        assertEquals(response, response2)
        assertEquals(1, processor.callCounter) // not increased
    }

    @Test
    @Ignore
    fun testGitDiffProcessor() {
        val gitClient = InvokingGitClient(File("/tmp/repo"))
        val processor: DiffProcessor = GitDiffProcessor(
            gitClient,
            ThombergsDiffParser(),
            UnifiedDiffConverter()
        )
        val context = DiffContext()
        val diff = processor.diff(Revisions("HEAD", "HEAD~"), context)
        assertNotNull(diff)
        assertTrue(diff.isNotEmpty())
    }
}