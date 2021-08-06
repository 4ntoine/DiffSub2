package com.eyeo.ctu.diffsub2

import fi.iki.elonen.NanoHTTPD
import java.lang.Exception

// Provide input for the external requests
interface DiffServer {
    fun setDiffProvider(processor: DiffProcessor)
    fun start()
    fun stop()
}

// DiffServer impl serving over HTTP with NanoHTTPD
class HttpDiffServer(
    val port: Int
) : DiffServer, NanoHTTPD(port) {
    private lateinit var processor: DiffProcessor
    override fun setDiffProvider(processor: DiffProcessor) {
        this.processor = processor
    }

    override fun start() {
        println("Start serving on port $port")
        super.start()
    }

    override fun stop() {
        println("Stopping")
        super.stop()
    }

    companion object {
        private const val FROM_ARG = "from"
        private const val CURRENT_ARG = "current"
    }

    override fun serve(session: IHTTPSession): Response {
        val fromRevision = session.parms[FROM_ARG]
        val currentRevision = session.parms[CURRENT_ARG]

        // validate
        if (fromRevision == null) {
            return newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                MIME_PLAINTEXT,
                """"$FROM_ARG" argument is required""")
        }

        if (currentRevision == null) {
            return newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                MIME_PLAINTEXT,
                """"$CURRENT_ARG" argument is required""")
        }

        try {
            val revisions = Revisions(fromRevision, currentRevision)
            val context = DiffContext()
            val diff = processor.diff(revisions, context)
            // Note: `context.headRevision` is set during the `diff()`
            val responseBuilder = StringBuilder(context.headRevision)
            // trivial mark-up: first line is HEAD, next - diff
            diff?.let { diff -> // it can be `null` if the client is already on the HEAD
                responseBuilder.append("\n")
                responseBuilder.append(diff)
            }
            return newFixedLengthResponse(responseBuilder.toString())
        } catch (e: Exception) {
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                e.message)
        }
    }
}