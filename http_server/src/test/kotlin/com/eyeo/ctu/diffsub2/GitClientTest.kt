package com.eyeo.ctu.diffsub2

import org.junit.Ignore
import org.junit.Test
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GitClientTest {
    private val gitClient: GitClient = InvokingGitClient(File("/tmp/repo"))

    @Test
    @Ignore
    fun testGitClientHead() {
        val headRevision = gitClient.headRevision()
        assertTrue(headRevision.isNotEmpty())
    }

    @Test
    @Ignore
    fun testGitClientDiff() {
        val diff = gitClient.diff(
            Revisions(
                "87998e1759898200e386db4e29c7706a865adcd0",
                "8df3d2ff755d3118c253cefc290c0adc65c1e2e7"
            ), DiffContext())
        assertNotNull(diff)
        assertTrue(diff.isNotEmpty())
    }
}