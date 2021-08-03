package com.eyeo.ctu.diffsub2

import kotlin.system.exitProcess

class ServerApp(
    private val parser: DiffParser,
    private val converter: Converter,
    private val sender: Sender
) {
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
            if (args.size != 3) {
                println("Usage: %connection% %topic% %action%=[create|send]")
                exitProcess(1)
            }

            val connection = args[0]
            val topic = args[1]
            val action = args[2]
            println("Connecting to \"$connection\", topic \"$topic\" ...")

            // wire
            val kafkaSender = KafkaSender(connection, topic)
            val app = ServerApp(
                ThombergsDiffParser(),
                GitLikeConverter(),
                kafkaSender
            )

            // process
            try {
                when (action) {
                    "send" -> {
                        println("Sending a message")
                        val input = generateSequence(::readLine).joinToString("\n")
                        app.onHook(input)
                    }
                    "create" -> {
                        println("Creating a topic")
                        kafkaSender.createTopic(topic)
                    }
                    else -> {
                        println("Unknown action: $action")
                        exitProcess(2)
                    }
                }
            } finally {
                app.stop()
            }
        }
    }
}