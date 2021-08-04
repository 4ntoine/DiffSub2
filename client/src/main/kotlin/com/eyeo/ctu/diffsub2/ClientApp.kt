package com.eyeo.ctu.diffsub2

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import java.util.*
import kotlin.system.exitProcess

class ClientApp(
    val receiver: Receiver,
    val converter: Converter,
    val filterManager: FilterManager
) {
    class Settings : ConnectionSettings() {
        @Parameter(
            names = ["-d", "-duration"],
            description = "Kafka Consumer poll duration (milliseconds)",
            required = true)
        var pollDurationMillis: Long? = null

        @Parameter(
            names = ["-g", "-group"],
            description = "Kafka consumer groupId (auto generated if not set)"
        )
        var groupId: String? = null

        @Parameter(
            names = ["-o", "-offset"],
            description = "Kafka topic offset (optional)"
        )
        var offset: Long? = null
    }

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
            var settings = Settings()
            val parser = JCommander
                .newBuilder()
                .addObject(settings)
                .build()
            try {
                parser.parse(*args)
            } catch (e: Exception) {
                println(e.message)
                parser.usage() // prints to stdout
                exitProcess(1)
            }

            println("Connecting to \"${settings.connection()}\", topic \"${settings.topic}\" and polling with interval ${settings.pollDurationMillis} millis ...")

            // auto fill missing optional fields
            // (it feels a bit strange as some fields are also auto set in Receiver)
            if (settings.groupId == null) {
                settings.groupId = UUID.randomUUID().toString()
                // otherwise only 1 client will receive the changes (competing consumers)
                println("Generated groupId = ${settings.groupId}")
            }

            // wire
            val app = ClientApp(
                KafkaReceiver(settings),
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