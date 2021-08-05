package com.eyeo.ctu.diffsub2

import fi.iki.elonen.NanoHTTPD
import java.lang.Exception

// Provide input for the external requests
interface DiffServer {
    fun setDiffProvider(provider: DiffProvider)
    fun start()
    fun stop()
}

// DiffServer impl serving over HTTP with NanoHTTPD
class HttpDiffServer(
    val port: Int
) : DiffServer, NanoHTTPD(port) {
    private lateinit var provider: DiffProvider
    override fun setDiffProvider(provider: DiffProvider) {
        this.provider = provider
    }

    override fun start() {
        println("Start serving on port $port")
        super.start()
    }

    override fun stop() {
        println("Stopping")
        super.stop()
    }

    override fun serve(session: IHTTPSession): Response {
        val fromRevision = session.parms["from"]
        val toRevision = session.parms["to"]

        // validate
        if (fromRevision == null) {
            return newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                MIME_PLAINTEXT,
                """"from" argument is required""")
        }

        try {
            val response = provider.diff(DiffRequest(fromRevision, toRevision))
            // not returning head revision to avoid encoding (we need to mark-up the body and the revision)
            return newFixedLengthResponse(response.diffBody)
        } catch (e: Exception) {
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                e.message)
        }
    }
}