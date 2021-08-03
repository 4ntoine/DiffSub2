package com.eyeo.ctu.diffsub2

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

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // wire
            val app = ServerApp(
                ThombergsDiffParser(),
                GitLikeConverter(),
                PrintingSender()
            )

            // process
            val input = generateSequence(::readLine).joinToString("\n")
            app.onHook(input)
        }
    }
}