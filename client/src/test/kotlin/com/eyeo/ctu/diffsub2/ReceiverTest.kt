package com.eyeo.ctu.diffsub2

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.lang.Thread.sleep
import java.time.Duration
import kotlin.test.assertTrue

@Ignore // For manual run only (required Kafka running)
class SenderTest {
    companion object {
        private const val TOPIC = "diffsub2"
    }

    private lateinit var receiver: KafkaReceiver
    private fun onMessage(offset: Long, message: ByteArray) {
        println("Got a message with offset $offset: \"${String(message)}\"")
    }

    @Before
    fun before() {
        // create a new sender before each test method to avoid interference
        receiver = KafkaReceiver("localhost:29092", Duration.ofMillis(100), TOPIC)
        receiver.setListener(::onMessage)
    }

    @After
    fun after() {
        receiver.disconnect()
        sleep(1_000) // let it actually disconnect
    }

    @Test
    fun testReceive() {
        receiver.start()
        sleep(5_000) // let if receive something
    }
}