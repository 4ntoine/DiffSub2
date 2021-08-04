package com.eyeo.ctu.diffsub2

import java.time.Duration
import kotlin.system.exitProcess

class ClientApp(
    val receiver: Receiver,
    val converter: Converter,
    val filterManager: FilterManager
) {
    private fun onMessage(offset: Long, message: ByteArray) {
        // TODO: this happens in KafkaReceiver thread, refactor to use a separate thread
        val diff = converter.convert(message)
        filterManager.apply(diff)
        println("Client topic offset is $offset") // TODO: write offset to Persistence
    }

    fun start() {
        // TODO: read offset from Persistence and pass to receiver
        receiver.setListener(::onMessage)
        receiver.start()
    }

    fun stop() {
        receiver.stop()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 3) {
                println("Usage: %connection% %topic% %poll_duration_millis% (%groupId%)")
                exitProcess(1)
            }

            val connection = args[0]
            val topic = args[1]
            val duration = Duration.ofMillis(args[2].toLong())
            println("Connecting to \"$connection\", topic \"$topic\" and polling with interval ${duration.toMillis()} millis ...")

            // optional consumer groupId
            val groupId: String? = if (args.size == 4) args[3] else null

            // wire
            val app = ClientApp(
                KafkaReceiver(connection, topic, duration, groupId),
                GitLikeConverter(),
                PrintingFilterManager()
            )

            app.start()
            println("Waiting for the changes ...")
            try {
                println("Press any key to finish")
                readLine()
            } finally {
                app.stop()
            }
        }
    }

}