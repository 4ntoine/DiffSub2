package com.eyeo.ctu.diffsub2

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import java.io.File
import kotlin.system.exitProcess

class ServerApp(
    private val processor: DiffProcessor,
    private val server: DiffServer
) {
    class Settings {
        @Parameter(
            names = ["-r", "-repo_path"],
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

        @Parameter(
            names = ["-c", "-cache_path"],
            description = "File system cache absolute path (using in-memory cache if not set)"
        )
        var cachePath: String? = null

        @Parameter(
            names = ["-j", "--json"],
            description = "Use JSON converter (using GitLikeConverter if not set)"
        )
        var jsonConverter: Boolean = false
    }

    init {
        server.setDiffProvider(processor)
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
            val gitRepoDir = File(settings.repoPath!!)
            if (!gitRepoDir.exists()) {
                println("Git repository path (${settings.repoPath}) does not exist!")
                exitProcess(2)
            }
            val gitClient = InvokingGitClient(gitRepoDir)
            val cache = if (settings.cachePath != null) {
                val cacheDir = File(settings.cachePath!!)
                if (!cacheDir.exists()) {
                    println("Cache path (${settings.cachePath}) does not exist!")
                    exitProcess(3)
                }
                println("Using file system cache (${settings.cachePath})")
                FileSystemDiffCache(cacheDir)
            } else {
                println("Using in-memory cache")
                InMemoryDiffCache()
            }
            val converter = if (settings.jsonConverter)
                JsonConverter()
            else
                UnifiedDiffConverter()
            val app = ServerApp(
                AddHeadDiffProcessor(                 // 1 - add HEAD info to the context
                    gitClient,
                    AlreadyOnHeadCheckDiffProcessor(  // 2 - check if the client is already on HEAD
                        CachingDiffProcessor(         // 3 - cache for better performance
                            cache,
                            GitDiffProcessor(         // 4 - actually request ...
                                gitClient,            // 5 - ... from Git
                                ThombergsDiffParser(),
                                converter
                            )
                        )
                    ),
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