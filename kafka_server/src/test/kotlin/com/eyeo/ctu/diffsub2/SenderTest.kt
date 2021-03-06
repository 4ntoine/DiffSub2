package com.eyeo.ctu.diffsub2

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.lang.Thread.sleep
import kotlin.test.assertTrue

@Ignore // For manual run only (requires Kafka running)
class SenderTest {
    companion object {
        private const val TOPIC = "diffsub2"
    }

    private lateinit var sender: KafkaSender

    @Before
    fun before() {
        // create a new sender before each test method to avoid interference
        val settings = ServerApp.Settings().apply {
            host = "localhost"
            port = 29092
            topic = TOPIC
        }
        sender = KafkaSender(settings)
    }

    @After
    fun after() {
        sender.stop()
    }

    @Test
    fun testCreateTopic() {
        sender.createTopic(TOPIC)
    }

    @Test
    fun testSend() {
        sender.send("Hello world".toByteArray())
    }

    @Test
    // the test can be useful for testing with ClientApp actually running and waiting for the events
    fun testSendRules() {
        sender.send("+Rule1\n-Rule2".toByteArray())
    }

    @Test
    fun testOffsetIsIncreasing() {
        for (i in 0..3) {
            val offset = sender.offset()
            sender.send("Message #$i".toByteArray())

            // send() is asynchronous, so let's wait some reasonable time
            // TODO: could be improved by introducing `waitForSent()` method or smth.
            sleep(1_000)

            val newOffset = sender.offset()
            assertTrue {
                // no offset before
                (offset == null && newOffset != null)
                ||
                // having offset before and it's increasing
                (offset != null && newOffset != null && newOffset > offset)
            }
        }
    }
}