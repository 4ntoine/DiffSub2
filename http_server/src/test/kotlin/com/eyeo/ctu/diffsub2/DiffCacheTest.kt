package com.eyeo.ctu.diffsub2

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiffCacheTest {
    @get:Rule
    var tmpFolder = TemporaryFolder()

    @Test
    fun testInMemoryCache() {
        val request = Revisions("from", "to")
        val diffBody = "body"
        val cache: DiffCache = InMemoryDiffCache()
        assertNull(cache.get(request))
        cache.put(request, diffBody)
        assertEquals(diffBody, cache.get(request))
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
        val request = Revisions("from1", "to1")
        assertNull(fileSystemCache.get(request))
        val expectedDiffBody = "body1"
        fileSystemCache.put(request, expectedDiffBody)
        assertTrue(fileSystemCache.getResponseFile(request).exists())
        val actualDiffBody = fileSystemCache.get(request)
        assertNotNull(actualDiffBody)
        assertEquals(expectedDiffBody, actualDiffBody)
        fileSystemCache.clear()
        assertNull(fileSystemCache.get(request))
    }
}