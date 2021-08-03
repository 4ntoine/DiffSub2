package com.eyeo.ctu.diffsub2

import org.junit.Test
import kotlin.test.assertEquals

private class TestReceiver : Receiver {
    private lateinit var listener: ReceiverListener
    private var offset = 0L

    override fun setListener(listener: ReceiverListener) {
        this.listener = listener
    }

    override fun start() {
        // nothing
    }

    override fun stop() {
        // nothing
    }

    fun feed(message: ByteArray) {
        this.listener(offset++, message)
    }
}

// we could use Mokito here, but implementing it just for simplicity
private class TestFilterManager : FilterManager {
    val diffs = mutableListOf<Diff>()

    override fun apply(diff: Diff) {
        diffs.add(diff)
    }
}

class ClientAppTest {

    @Test
    fun testFullFlow() {
        // wire
        val receiver = TestReceiver()
        val filterManager = TestFilterManager()

        val app = ClientApp(
            receiver,
            GitLikeConverter(),
            filterManager
        )
        app.start()

        try {
            receiver.feed("+Rule1\n-Rule2".toByteArray())
            assertEquals(1, filterManager.diffs.size)
            val actualDiff = filterManager.diffs[0]
            assertEquals(1, actualDiff.add.size)
            assertEquals("Rule1", actualDiff.add[0])
            assertEquals(1, actualDiff.remove.size)
            assertEquals("Rule2", actualDiff.remove[0])
        } finally {
            app.stop()
        }
    }
}