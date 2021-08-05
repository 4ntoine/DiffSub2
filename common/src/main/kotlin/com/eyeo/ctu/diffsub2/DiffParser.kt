package com.eyeo.ctu.diffsub2

import io.reflectoring.diffparser.api.UnifiedDiffParser
import io.reflectoring.diffparser.api.model.Line

// parses UnifiedDiff produced by Git (git diff)
interface DiffParser {
    fun parse(input: String): Diff
}

// DiffParser impl that uses Thombergs library
class ThombergsDiffParser : DiffParser {
    private val parser = UnifiedDiffParser()
    override fun parse(input: String): Diff {
        val diffList = parser.parse(input.byteInputStream())
        val addRules = mutableListOf<String>()
        val removeRules = mutableListOf<String>()
        for (eachDiff in diffList) {
            for (eachHunk in eachDiff.hunks) {
                for (eachLine in eachHunk.lines) {
                    when (eachLine.lineType) {
                        Line.LineType.TO   -> addRules.add(eachLine.content)
                        Line.LineType.FROM -> removeRules.add(eachLine.content)
                        else               -> { /* ignored */ }
                    }
                }
            }
        }
        return Diff(addRules, removeRules)
    }
}