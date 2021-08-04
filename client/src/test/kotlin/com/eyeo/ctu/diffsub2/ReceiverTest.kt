package com.eyeo.ctu.diffsub2

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.lang.Thread.sleep
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.fail

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
        val settings = ClientApp.Settings().apply {
            host = "localhost"
            port = 29092
            topic = TOPIC
            pollDurationMillis = 100
        }
        receiver = KafkaReceiver(settings)
        receiver.setListener(::onMessage)
    }

    @After
    fun after() {
        receiver.stop()
        sleep(1_000) // let it actually disconnect
    }

    @Test
    fun testReceive() {
        receiver.start()
        sleep(5_000) // let if receive something
    }

    @Test
    fun testReceiveConnectionErrorReported() {
        val invalidPort = 10240L + Random.nextInt(1024).absoluteValue
        val settings = ClientApp.Settings().apply {
            host = "localhost"
            port = invalidPort // invalid!
            topic = TOPIC
            pollDurationMillis = 100
        }
        try {
            KafkaReceiver(settings).start()
            fail("Received is expected to report a connection error")
        } catch (e: Exception) {
            // expected
        }
    }
}