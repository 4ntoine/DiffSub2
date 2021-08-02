package com.eyeo.ctu.diffsub2

import java.io.OutputStream

// Sender impl that send into the output
class OutputSender (
    private val output: OutputStream
) : Sender {
    override fun send(content: ByteArray) {
        output.write(content)
    }
}