package com.eyeo.ctu.diffsub2

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
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
    fun stop()
}

// Sender impl for Apache Kafka
class KafkaReceiver(
    // Ideally introduce and use `ReceiverSettings` here.
    // However it fully corresponds to `ClientApp.Setting` and being pragmatic for POC i consider it ok.
    val settings: ClientApp.Settings,
    properties: Properties? = null,
) : Receiver {

    private val allProperties: Properties
    private lateinit var consumer: Consumer<String, ByteArray>
    private val signalClose = AtomicBoolean(false)
    private lateinit var listener: ReceiverListener

    companion object {
        private const val BOOTSTRAP_SERVERS = "bootstrap.servers"
    }

    init {
        // adjust properties
        allProperties = if (properties != null) Properties(properties) else Properties()

        // connection
        allProperties[BOOTSTRAP_SERVERS] = settings.connection()

        // details
        allProperties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        allProperties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java.name
        allProperties[ConsumerConfig.GROUP_ID_CONFIG] = settings.groupId
        allProperties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        if (ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG !in allProperties) {
            // needed to check the connection (ctor does not throw an exception)
            allProperties[ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG] = 5_000 // 5 seconds
        }
    }

    override fun setListener(listener: ReceiverListener) {
        this.listener = listener
    }

    override fun start() {
        consumer = KafkaConsumer(allProperties)
        consumer.listTopics() // to force it throw a connection exception if not connected
        consumer.subscribe(listOf(settings.topic))
        thread(start = true) {
            consumer.use { consumer ->
                while (!signalClose.get()) {
                    // polling! TODO: how to switch to long polling?
                    try {
                        for (eachRecord in consumer.poll(Duration.ofMillis(settings.pollDurationMillis!!))) {
                            this.listener(eachRecord.offset(), eachRecord.value())
                        }
                    } catch (e: WakeupException) {
                        if (!signalClose.get())
                            throw e
                    }
                }
            }
        }
    }

    // is asynchronous (not waiting for actual consumer closed)
    override fun stop() {
        // we could make it synchronous
        if (this::consumer.isInitialized) {
            signalClose.set(true)
            consumer.wakeup() // to shake it in cas of long poll interval
        }
    }
}