package com.eyeo.ctu.diffsub2

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import org.junit.Test
import kotlin.test.assertEquals

class ConnectionSettingsTest {

    companion object {
        private const val HOST = "localhost"
        private const val PORT = "29092"
        private const val TOPIC = "diffsub"
    }

    private fun parse(vararg args: String): ConnectionSettings {
        val settings = ConnectionSettings()
        JCommander.newBuilder().addObject(settings).build().parse(*args)
        return settings
    }

    @Test
    fun testParse() {
        val settings = parse("-h", HOST, "-p", PORT, "-t", TOPIC)
        assertEquals(HOST, settings.host)
        assertEquals(PORT.toLong(), settings.port)
        assertEquals(TOPIC, settings.topic)
    }

    @Test(expected = ParameterException::class)
    fun testParseWithoutRequiresThrows() {
        parse(/*"-h", HOST,*/ "-p", PORT, "-t", TOPIC)
    }

    @Test(expected = ParameterException::class)
    fun testParseInvalidPortRequiresThrows() {
        parse("-h", HOST, "-p", "not_a_long", "-t", TOPIC)
    }
}