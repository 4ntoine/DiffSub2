package com.eyeo.ctu.diffsub2

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiffCacheTest {
    @get:Rule
    var tmpFolder = TemporaryFolder()

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

    private lateinit var fileSystemCache: FileSystemDiffCache

    @Before
    fun before() {
        // have to do it in `@Before` as tmpFodler is not yet created
        fileSystemCache = FileSystemDiffCache(tmpFolder.root)
    }

    @Test
    fun testFileSystemCache() {
        fileSystemCache.clear()
        val request = DiffRequest("from1", "to1")
        assertNull(fileSystemCache.get(request))
        val expectedResponse = DiffResponse("body1", request.toRevision!!)
        fileSystemCache.put(request, expectedResponse)
        assertTrue(fileSystemCache.getResponseFile(request).exists())
        val actualResponse = fileSystemCache.get(request)
        assertNotNull(actualResponse)
        assertEquals(expectedResponse.diffBody, actualResponse.diffBody)
        assertEquals(expectedResponse.headRevision, actualResponse.headRevision)
        fileSystemCache.clear()
        assertNull(fileSystemCache.get(request))
    }
}