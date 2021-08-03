package com.eyeo.ctu.diffsub2

import org.junit.Test
import kotlin.test.assertEquals

class ConverterTest {
    private val converter: Converter = GitLikeConverter()

    // Note: the test assumes some rules order in Diff though it's not guaranteed by the impl
    // TODO: refactor to ignore the rules order

    @Test
    fun testAddSingleToMessage() {
        val output = converter.convert(Diff(listOf("Rule1"), emptyList()))
        assertEquals("+Rule1", String(output))
    }

    @Test
    fun testAddMultipleToMessage() {
        val output = converter.convert(Diff(listOf("Rule1", "Rule2"), emptyList()))
        assertEquals("+Rule1\n+Rule2", String(output))
    }

    @Test
    fun testRemoveSingleToMessage() {
        val output = converter.convert(Diff(emptyList(), listOf("Rule1")))
        assertEquals("-Rule1", String(output))
    }

    @Test
    fun testRemoveMultipleToMessage() {
        val output = converter.convert(Diff(emptyList(), listOf("Rule1", "Rule2")))
        assertEquals("-Rule1\n-Rule2", String(output))
    }

    @Test
    fun testAddRemoveToMessage() {
        val output = converter.convert(Diff(listOf("Rule1"), listOf("Rule2")))
        assertEquals("-Rule2\n+Rule1", String(output))
    }

    @Test
    fun testAddSingleFromMessage() {
        val actualDiff = converter.convert("+Rule1".toByteArray())
        assertEquals(1, actualDiff.add.size)
        assertEquals("Rule1", actualDiff.add[0])
        assertEquals(0, actualDiff.remove.size)
    }

    @Test
    fun testAddMultipleFromMessage() {
        val actualDiff = converter.convert("+Rule1\n+Rule2".toByteArray())
        assertEquals(2, actualDiff.add.size)
        assertEquals("Rule1", actualDiff.add[0])
        assertEquals("Rule2", actualDiff.add[1])
        assertEquals(0, actualDiff.remove.size)
    }

    @Test
    fun testRemoveSingleFromMessage() {
        val actualDiff = converter.convert("-Rule1".toByteArray())
        assertEquals(0, actualDiff.add.size)
        assertEquals(1, actualDiff.remove.size)
        assertEquals("Rule1", actualDiff.remove[0])
    }

    @Test
    fun testRemoveMultipleFromMessage() {
        val actualDiff = converter.convert("-Rule1\n-Rule2".toByteArray())
        assertEquals(0, actualDiff.add.size)
        assertEquals(2, actualDiff.remove.size)
        assertEquals("Rule1", actualDiff.remove[0])
        assertEquals("Rule2", actualDiff.remove[1])
    }

    @Test
    fun testEmptyFromMessage() {
        val actualDiff = converter.convert("".toByteArray())
        assertEquals(0, actualDiff.add.size)
        assertEquals(0, actualDiff.remove.size)
    }
}