package com.eyeo.ctu.diffsub2

import org.junit.Test
import kotlin.test.assertEquals

class ConverterTest {
    private val converter: Converter = GitLikeConverter()

    @Test
    fun testAddSingle() {
        val output = converter.encode(Diff(listOf("Rule1"), emptyList()))
        assertEquals("+Rule1", String(output))
    }

    @Test
    fun testAddMultiple() {
        val output = converter.encode(Diff(listOf("Rule1", "Rule2"), emptyList()))
        assertEquals("+Rule1\n+Rule2", String(output))
    }

    @Test
    fun testRemoveSingle() {
        val output = converter.encode(Diff(emptyList(), listOf("Rule1")))
        assertEquals("-Rule1", String(output))
    }

    @Test
    fun testRemoveMultiple() {
        val output = converter.encode(Diff(emptyList(), listOf("Rule1", "Rule2")))
        assertEquals("-Rule1\n-Rule2", String(output))
    }

    @Test
    fun testAddRemove() {
        val output = converter.encode(Diff(listOf("Rule1"), listOf("Rule2")))
        assertEquals("-Rule2\n+Rule1", String(output))
    }
}