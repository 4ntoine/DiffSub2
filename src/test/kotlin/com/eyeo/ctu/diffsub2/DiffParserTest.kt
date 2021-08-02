package com.eyeo.ctu.diffsub2

import org.junit.Test
import kotlin.test.assertEquals

class DiffParserTest {
    private val parser: DiffParser = ThombergsDiffParser()

    @Test
    fun testSingleAddLine() {
        val diffLines = """
        commit 5f5b3f27b7abaadec06abad523b6aab59a020f4a (HEAD -> master, origin/master)
        Author: Anton Smirnov <a.smirnov@eyeo.com>
        Date:   Sat Jul 31 00:40:21 2021 +0500

            Add "Rule1".

        diff --git a/easylist.txt b/easylist.txt
        index e69de29..8eb8f8d 100644
        --- a/easylist.txt
        +++ b/easylist.txt
        @@ -0,0 +1 @@
        +Rule1.
        \ No newline at end of file
        """.trimIndent()
        val diff = parser.parse(diffLines)
        assertEquals(1, diff.add.size)
        assertEquals("Rule1.", diff.add[0])
        assertEquals(0, diff.remove.size)
    }

    @Test
    fun testSingleRemoveLine() {
        val diffLines = """
        commit 5f5b3f27b7abaadec06abad523b6aab59a020f4a (HEAD -> master, origin/master)
        Author: Anton Smirnov <a.smirnov@eyeo.com>
        Date:   Sat Jul 31 00:40:21 2021 +0500

            Add "Rule1".

        diff --git a/easylist.txt b/easylist.txt
        index e69de29..8eb8f8d 100644
        --- a/easylist.txt
        +++ b/easylist.txt
        @@ -0,0 +1 @@
        -Rule1.
        \ No newline at end of file
        """.trimIndent()
        val diff = parser.parse(diffLines)
        assertEquals(0, diff.add.size)
        assertEquals(1, diff.remove.size)
        assertEquals("Rule1.", diff.remove[0])
    }

    @Test
    fun testMultipleAddLines() {
        val diffLines = """
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
        +Rule2.
        \ No newline at end of file
        """.trimIndent()
        val diff = parser.parse(diffLines)
        assertEquals(2, diff.add.size)
        assertEquals("Rule1.", diff.add[0])
        assertEquals("Rule2.", diff.add[1])
        assertEquals(0, diff.remove.size)
    }

    @Test
    fun testMultipleRemoveLines() {
        val diffLines = """
        commit 5f5b3f27b7abaadec06abad523b6aab59a020f4a (HEAD -> master, origin/master)
        Author: Anton Smirnov <a.smirnov@eyeo.com>
        Date:   Sat Jul 31 00:40:21 2021 +0500

            Add "Rule1".

        diff --git a/easylist.txt b/easylist.txt
        index e69de29..8eb8f8d 100644
        --- a/easylist.txt
        +++ b/easylist.txt
        @@ -0,0 +2 @@
        -Rule1.
        -Rule2.
        \ No newline at end of file
        """.trimIndent()
        val diff = parser.parse(diffLines)
        assertEquals(0, diff.add.size)
        assertEquals(2, diff.remove.size)
        assertEquals("Rule1.", diff.remove[0])
        assertEquals("Rule2.", diff.remove[1])
    }

    @Test
    fun testBothAddAndRemoveLines() {
        val diffLines = """
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
        val diff = parser.parse(diffLines)
        assertEquals(1, diff.add.size)
        assertEquals("Rule1.", diff.add[0])
        assertEquals(1, diff.remove.size)
        assertEquals("Rule2.", diff.remove[0])
    }
}