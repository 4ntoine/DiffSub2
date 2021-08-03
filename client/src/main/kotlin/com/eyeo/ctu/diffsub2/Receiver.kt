package com.eyeo.ctu.diffsub2

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

typealias ReceiverListener = (/* offset */ Long, /* message */ ByteArray) -> Unit

// receives a message to the broker
interface Receiver {
    fun setListener(listener: ReceiverListener)
    fun start() // requires listener to be set with `setListener()`
}

// Sender impl for Apache Kafka
class KafkaReceiver(
    val connection: String, // eg. "localhost:9092"
    val pollDuration: Duration,
    val topic: String,
    properties: Properties? = null
) : Receiver {

    private val consumer: Consumer<String, ByteArray>
    private val signalClose = AtomicBoolean(false)
    private lateinit var listener: ReceiverListener

    companion object {
        private const val BOOTSTRAP_SERVERS = "bootstrap.servers"
    }

    init {
        // adjust properties
        val allProperties = if (properties != null) Properties(properties) else Properties()

        // connection
        allProperties[BOOTSTRAP_SERVERS] = connection

        // details
        allProperties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        allProperties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java.name
        allProperties[ConsumerConfig.GROUP_ID_CONFIG] = "demo-consumer-1"
        allProperties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        consumer = KafkaConsumer(allProperties)
    }

    override fun setListener(listener: ReceiverListener) {
        this.listener = listener
    }

    override fun start() {
        consumer.subscribe(listOf(topic))
        thread(start = true) {
            consumer.use { consumer ->
                while (!signalClose.get()) {
                    // polling! TODO: how to switch to long polling?
                    for (eachRecord in consumer.poll(pollDuration)) {
                        this.listener(eachRecord.offset(), eachRecord.value())
                    }
                }
            }
        }
    }

    // is asynchronous (not waiting for actual consumer closed)
    fun disconnect() {
        // we could make it synchronous
        signalClose.set(true)
    }
}