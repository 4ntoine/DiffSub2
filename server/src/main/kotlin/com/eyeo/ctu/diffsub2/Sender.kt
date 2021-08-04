package com.eyeo.ctu.diffsub2

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

// sends a message to the broker
interface Sender {
    fun send(content: ByteArray) // asynchronously
    fun stop()
}

// Sender impl that just print to standard output
// (for instance for debugging)
class PrintingSender : Sender {
    override fun send(content: ByteArray) {
        println(String(content))
    }

    override fun stop() {
        // nothing
    }
}

// Sender impl for Apache Kafka
class KafkaSender(
    // Ideally introduce and use `ReceiverSettings` here.
    // However it fully corresponds to `ServerApp.Setting` and being pragmatic for POC i consider it ok.
    val settings: ServerApp.Settings,
    properties: Properties? = null
) : Sender {

    private val producer: Producer<String, ByteArray>
    private var offset: AtomicLong? = null // synchronized with `offsetLock`
    private var offsetLock = ReentrantReadWriteLock()

    companion object {
        private const val BOOTSTRAP_SERVERS = "bootstrap.servers"
    }

    init {
        // adjust properties
        val allProperties = if (properties != null) Properties(properties) else Properties()

        // connection
        allProperties[BOOTSTRAP_SERVERS] = settings.connection()

        // details
        allProperties[ProducerConfig.ACKS_CONFIG] = "all"
        allProperties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        allProperties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java.name

        producer = KafkaProducer(allProperties)
    }

    private fun sendCallback(metadata: RecordMetadata?, exception: Exception?) {
        exception?.let {
            println("Failed to send to the \"${settings.topic}\": $it")
            return
        }

        // thread-safe
        metadata?.let {
            offsetLock.write {
                if (offset == null) {
                    offset = AtomicLong(0)
                }
                offset!!.set(it.offset())
                println("Server topic offset is $offset")
            }
        }
    }

    fun createTopic(topic: String) {
        val newTopic = NewTopic(topic, Optional.empty(), Optional.empty())
        try {
            val properties = Properties()
            properties[BOOTSTRAP_SERVERS] = settings.connection()
            AdminClient.create(properties).use { adminClient ->
                adminClient
                    .createTopics(listOf(newTopic))
                    .all()
                    .get()
            }
        } catch (e: InterruptedException) {
            // Ignore if TopicExistsException, which may be valid if topic exists
            if (e.cause !is TopicExistsException) {
                throw RuntimeException(e)
            }
        } catch (e: ExecutionException) {
            if (e.cause !is TopicExistsException) {
                throw RuntimeException(e)
            }
        }
    }

    override fun send(content: ByteArray) {
        producer.send(ProducerRecord(settings.topic, "key", content), ::sendCallback)
        producer.flush()
    }

    fun offset(): Long? {
        offsetLock.read {
            return offset?.let { it.get() }
        }
    }

    override fun stop() {
        producer.close() // it will wait for send() actually finished
    }
}