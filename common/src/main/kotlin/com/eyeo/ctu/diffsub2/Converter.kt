package com.eyeo.ctu.diffsub2

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.io.InputStreamReader

// converts between Diff and Broker message content
interface Converter {
    fun convert(diff: Diff): ByteArray
    fun convert(input: ByteArray): Diff
}

// Converter impl that uses mark-up similar to Git:
// "+" for added rules
// "-" for removed rules
class UnifiedDiffConverter : Converter {
    companion object {
        private const val ADD_TOKEN = "+"
        private const val REMOVE_TOKEN = "-"
        private const val SEPARATOR = "\n"
    }

    override fun convert(diff: Diff): ByteArray {
        val output = mutableListOf<String>()
        for (eachRemoveRule in diff.remove) {
            output.add("$REMOVE_TOKEN$eachRemoveRule")
        }
        for (eachAddRule in diff.add) {
            output.add("$ADD_TOKEN$eachAddRule")
        }
        return output
            .joinToString(SEPARATOR)
            .toByteArray()
    }

    override fun convert(input: ByteArray): Diff {
        val lines = String(input).split(SEPARATOR)
        val addRules = mutableListOf<String>()
        val removeRules = mutableListOf<String>()
        for (eachLine in lines) {
            if (eachLine.isEmpty()) {
                // TODO: ignore/throw?
                continue
            }
            val rule = eachLine.substring(1)
            when {
                eachLine.startsWith(ADD_TOKEN)    -> addRules.add(rule)
                eachLine.startsWith(REMOVE_TOKEN) -> removeRules.add(rule)
                else                              -> { /* ignored */ }
            }
        }
        return Diff(addRules, removeRules)
    }
}

// Converter impl to JSON using Gson
class JsonConverter : Converter {

    // introduced intentionally to abstract from the naming changes,
    // (GSON uses reflection)
    private data class Filters(
        @SerializedName(value = "add")
        val add: List<String>,

        @SerializedName(value = "remove")
        val remove: List<String>
    )

    private data class Changes(
        @SerializedName(value = "filters")
        val filters: Filters
    )

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun convert(diff: Diff): ByteArray {
        val changes = Changes(Filters(diff.add, diff.remove))
        return gson.toJson(changes).toByteArray()
    }

    override fun convert(input: ByteArray): Diff {
        val changes = gson.fromJson(
            InputStreamReader(input.inputStream()),
            Changes::class.java)
        return Diff(changes.filters.add, changes.filters.remove)
    }
}