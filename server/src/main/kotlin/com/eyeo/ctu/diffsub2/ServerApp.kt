package com.eyeo.ctu.diffsub2

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
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