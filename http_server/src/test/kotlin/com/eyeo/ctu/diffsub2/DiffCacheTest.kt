package com.eyeo.ctu.diffsub2

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DiffCacheTest {
    @Test
    fun testInMemoryCache() {
        val request = DiffRequest("from", "to")
        val response = DiffResponse("body", "head")
        val cache: DiffCache = InMemoryDiffCache()
        assertNull(cache.get(request))
        cache.put(request, response)
        assertEquals(response, cache.get(request))
        cache.clear()
        assertNull(cache.get(request))
    }
}