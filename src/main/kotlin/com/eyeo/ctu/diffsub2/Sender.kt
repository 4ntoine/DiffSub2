package com.eyeo.ctu.diffsub2

// sends a message to the broker
interface Sender {
    fun send(content: ByteArray)
}

// Sender impl that just print to standard output
// (for instance for debugging)
class PrintingSender : Sender {
    override fun send(content: ByteArray) {
        println(String(content))
    }
}