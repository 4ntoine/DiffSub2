package com.eyeo.ctu.diffsub2

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import java.util.*
import kotlin.system.exitProcess

class ServerApp(
    private val parser: DiffParser,
    private val converter: Converter,
    private val sender: Sender
) {
    class Settings : ConnectionSettings() {
        @Parameter(
            names = ["-a", "-action"],
            description = """Action ("create" - create the topic, "send" - send stdin)""",
            required = true
        )
        var action: String? = null

        // TODO: probably makes sense to pass key generator classname
        // (interface + concrete impl classname) for some advanced cases.
        // Seems to be not needed in POC.
        @Parameter(
            names = ["-k", "-key"],
            description = "Kafka message key (auto generated from topic name if not set)"
        )
        var key: String? = null
    }

    fun onHook(diffInput: String) {
        val diff = parser.parse(diffInput)
        val message = converter.convert(diff)
        sender.send(message)
    }

    fun stop() {
        sender.stop()
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
            println("Connecting to \"${settings.connection()}\", topic \"${settings.topic}\" ...")

            // auto fill missing optional fields
            // (it feels a bit strange as some fields could be auto set in Sender)
            if (settings.key == null) {
                settings.key = "${settings.topic}_key"
                println("Generated message key = ${settings.key})")
            }

            // wire
            val kafkaSender = KafkaSender(settings)
            val app = ServerApp(
                ThombergsDiffParser(),
                GitLikeConverter(),
                kafkaSender
            )

            // process
            try {
                when (settings.action) {
                    "send" -> {
                        println("Sending a message")
                        val input = generateSequence(::readLine).joinToString("\n")
                        app.onHook(input)
                    }
                    "create" -> {
                        println("Creating a topic")
                        kafkaSender.createTopic(settings.topic!!)
                    }
                    else -> {
                        println("Unknown action: ${settings.action}")
                        exitProcess(2)
                    }
                }
            } finally {
                app.stop()
            }
        }
    }
}