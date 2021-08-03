package com.eyeo.ctu.diffsub2

// converts between Diff and Broker message content
interface Converter {
    fun encode(diff: Diff): ByteArray
}

// Converter impl that uses mark-up similar to Git:
// "+" for added rules
// "-" for removed rules
class GitLikeConverter : Converter {
    override fun encode(diff: Diff): ByteArray {
        val output = mutableListOf<String>()
        for (eachRemoveRule in diff.remove) {
            output.add("-$eachRemoveRule")
        }
        for (eachAddRule in diff.add) {
            output.add("+$eachAddRule")
        }
        return output
            .joinToString("\n")
            .toByteArray()
    }
}