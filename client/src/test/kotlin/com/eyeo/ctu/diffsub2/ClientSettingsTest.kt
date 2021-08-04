package com.eyeo.ctu.diffsub2

import com.beust.jcommander.JCommander
import org.junit.Test
import kotlin.test.assertEquals

class ClientSettingsTest {
    companion object {
        private const val HOST = "localhost"
        private const val PORT = "29092"
        private const val TOPIC = "diffsub"
        private const val DURATION = "100"
        private const val GROUP = "groupId"
    }

    private fun parse(vararg args: String): ClientApp.Settings {
        val settings = ClientApp.Settings()
        JCommander.newBuilder().addObject(settings).build().parse(*args)
        return settings
    }

    @Test
    fun testParse() {
        val settings = parse(
            // inherited from ConnectionSettings
            "-h", HOST,
            "-p", PORT,
            "-t", TOPIC,

            // specific
            "-d", DURATION,
            "-g", GROUP)
        assertEquals(HOST, settings.host)
        assertEquals(PORT.toLong(), settings.port)
        assertEquals(TOPIC, settings.topic)
        assertEquals(DURATION.toLong(), settings.pollDurationMillis)
        assertEquals(GROUP, settings.groupId)
    }
}