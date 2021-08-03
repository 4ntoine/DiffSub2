package com.eyeo.ctu.diffsub2

// applies filters diff
interface FilterManager {
    fun apply(diff: Diff)
}

// FilterManager impl that just prints to std output
class PrintingFilterManager : FilterManager {
    override fun apply(diff: Diff) {
        for (eachRemoveRule in diff.remove) {
            println("Removing rule: $eachRemoveRule")
        }
        for (eachAddRule in diff.add) {
            println("Adding rule: $eachAddRule")
        }
    }
}