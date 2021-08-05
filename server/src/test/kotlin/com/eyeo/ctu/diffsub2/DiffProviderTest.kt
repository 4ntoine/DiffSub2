package com.eyeo.ctu.diffsub2

import org.junit.Test
import java.io.File
import kotlin.test.*

class DiffProviderTest {

    // could be stubbed with Mockito
    private class TestGitClient(
        private val revision: String,
        private val diff: String
    ) : GitClient {
        override fun getHeadRevision(): String  = revision
        override fun getDiff(fromRevision: String, toRevision: String): String = diff
    }

    private class TestDiffProvider(
        private val diffResponse: DiffResponse
    ) : DiffProvider {
        lateinit var request: DiffRequest
        override fun diff(request: DiffRequest): DiffResponse {
            this.request = request
            return diffResponse
        }
    }

    @Test
    fun testToRevisionAddsIfMissing() {
        val revision = "headRevision"
        val gitClient = TestGitClient(revision, "")
        val testProvider = TestDiffProvider(DiffResponse("", ""))
        val provider = ToRevisionDiffProvider(gitClient, testProvider)
        provider.diff(DiffRequest("fromRevision", null)) // not providing toRevision
        assertEquals(revision, testProvider.request.toRevision)
    }

    @Test
    fun testToRevisionDoesNotAddIfPresented() {
        val headRevision = "headRevision"
        val revision = "toRevision"
        val gitClient = TestGitClient(headRevision, "")
        val testProvider = TestDiffProvider(DiffResponse("", ""))
        val provider = ToRevisionDiffProvider(gitClient, testProvider)
        provider.diff(DiffRequest("fromRevision", revision)) // providing
        assertEquals(revision, testProvider.request.toRevision) // shoudld not change
    }

    @Test
    @Ignore
    fun testGitDiffProvider() {
        val gitClient = InvokingGitClient(File("/tmp/repo"))
        val provider: DiffProvider = GitDiffProvider(
            gitClient,
            ThombergsDiffParser(),
            GitLikeConverter()
        )
        val diffResponse = provider.diff(DiffRequest("HEAD", "HEAD~"))
        assertTrue(diffResponse.diffBody.isNotEmpty())
    }
}