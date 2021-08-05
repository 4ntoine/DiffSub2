package com.eyeo.ctu.diffsub2

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import java.io.File
import kotlin.system.exitProcess

class ServerApp(
    private val provider: DiffProvider,
    private val server: DiffServer
) {
    class Settings {
        @Parameter(
            names = ["-r", "-repo"],
            description = "Git repository absolute path",
            required = true
        )
        var repoPath: String? = null

        @Parameter(
            names = ["-p", "-port"],
            description = "HTTP server port",
            required = true
        )
        var port: Int? = null
    }

    init {
        server.setDiffProvider(provider)
    }

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            var settings = Settings()
            val parser = JCommander
                .newBuilder()
                .addObject(settings)
                .build()
            try {
                parser.parse(*args)
            } catch (e: Exception) {
                println(e.message)
                parser.usage() // prints to stdout
                exitProcess(1)
            }

            // wire
            val gitRepoPath = File(settings.repoPath!!)
            if (!gitRepoPath.exists()) {
                println("Git repository does not exist!")
                exitProcess(2)
            }
            val gitClient = InvokingGitClient(gitRepoPath)
            val app = ServerApp(
                ToRevisionDiffProvider( // 1 - add 'ToRevision' if it's missing
                    gitClient,
                    CachingDiffProvider( // 2 - cache for better performance
                        InMemoryDiffCache(),
                        GitDiffProvider( // 3 - actually request Git about the changes
                            InvokingGitClient(gitRepoPath),
                            ThombergsDiffParser(),
                            GitLikeConverter()
                        )
                    )
                ),
                HttpDiffServer(settings.port!!)
            )

            app.start()
            println("Ready")
            try {
                println("Press any key to finish")
                readLine()
            } finally {
                app.stop()
            }
        }
    }
}