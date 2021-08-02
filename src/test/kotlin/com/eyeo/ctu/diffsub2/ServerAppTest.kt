package com.eyeo.ctu.diffsub2

import org.junit.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServerAppTest {
    @Test
    fun testFullFlow() {
        val diffInput = """
        commit 5f5b3f27b7abaadec06abad523b6aab59a020f4a (HEAD -> master, origin/master)
        Author: Anton Smirnov <a.smirnov@eyeo.com>
        Date:   Sat Jul 31 00:40:21 2021 +0500

            Add "Rule1".

        diff --git a/easylist.txt b/easylist.txt
        index e69de29..8eb8f8d 100644
        --- a/easylist.txt
        +++ b/easylist.txt
        @@ -0,0 +2 @@
        +Rule1.
        -Rule2.
        \ No newline at end of file
        """.trimIndent()
        val output = ByteArrayOutputStream()
        val app = ServerApp(
            ThombergsDiffParser(),
            GitLikeConverter(),
            OutputSender(output)
        )
        app.onHook(diffInput)
        // we don't care about the order (not guaranteed by Converter)
        val actualOutputLines = String(output.toByteArray()).split("\n")
        assertEquals(2, actualOutputLines.size)
        assertTrue(actualOutputLines.contains("-Rule2."))
        assertTrue(actualOutputLines.contains("+Rule1."))
    }
}