package com.eyeo.ctu.diffsub2

import com.beust.jcommander.Parameter

open class ConnectionSettings {
    @Parameter(
        names = ["-h", "-host"],
        description = "Kafka host",
        required = true)
    var host: String? = null

    @Parameter(
        names = ["-p", "-port"],
        description = "Kafka port",
        required = true)
    var port: Long? = null

    @Parameter(
        names = ["-t", "-topic"],
        description = "Kafka topic",
        required = true)
    var topic: String? = null

    // Kafka connection string
    fun connection() = "$host:$port"
}