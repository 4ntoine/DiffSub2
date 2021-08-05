package com.eyeo.ctu.diffsub2

// saves responses for requests
interface DiffCache {
    // returns `null` if having nothing (no separate `has()`)
    fun get(request: DiffRequest): DiffResponse?
    fun put(request: DiffRequest, response: DiffResponse)
    fun clear()
}

// DiffCache impl that keeps everything in memory
class InMemoryDiffCache : DiffCache {
    private val map = mutableMapOf<DiffRequest, DiffResponse>()

    override fun clear() {
        map.clear()
    }

    override fun get(request: DiffRequest): DiffResponse? {
        return map[request]
    }

    override fun put(request: DiffRequest, response: DiffResponse) {
        map[request] = response
    }
}