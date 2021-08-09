package com.eyeo.ctu.diffsub2

import java.io.File

// execute the command line and return the process output (stdout)
fun execute(command: Array<String>): List<String> {
    val p = Runtime.getRuntime().exec(command)
    val output = p.inputStream.bufferedReader().readLines()
    val resultCode = p.waitFor()
    if (resultCode != 0) {
        throw Exception("Process ${command.joinToString(" ")} result code is not 0")
    }
    return output
}

// Git client
interface GitClient : DiffProcessor {
    fun headRevision(): String
}

// GitClient impl that launches Git binary via command line
class InvokingGitClient(
    private val gitRepoPath: File,
    private val ignoreSpaceChangeLines: Boolean = true
) : GitClient {

    private fun executeGit(cmd: Array<String>): List<String> {
        val allCmd = mutableListOf<String>()
        allCmd.addAll(listOf("git", "--git-dir=${gitRepoPath.absolutePath}"))
        allCmd.addAll(cmd)
        return execute(allCmd.toTypedArray())
    }

    override fun headRevision(): String {
        val output = executeGit(arrayOf("rev-parse", "HEAD"))
        return output.first().trim()
    }

    override fun diff(revisions: Revisions, /* not actually used */ context: DiffContext): String {
        val spaceChangeArg = if (ignoreSpaceChangeLines) "-w" else ""
        val output = executeGit(arrayOf("diff", spaceChangeArg, revisions.from, revisions.to))
        return output.joinToString("\n")
    }
}