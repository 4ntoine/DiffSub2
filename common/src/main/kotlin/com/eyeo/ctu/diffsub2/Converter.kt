package com.eyeo.ctu.diffsub2

// converts between Diff and Broker message content
interface Converter {
    fun convert(diff: Diff): ByteArray
    fun convert(input: ByteArray): Diff
}

// Converter impl that uses mark-up similar to Git:
// "+" for added rules
// "-" for removed rules
class GitLikeConverter : Converter {
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