package net.liyze.basin.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.IOException
import java.nio.file.Files
import java.util.*

/**
 * Basin Config Bean
 */
class Config {
    @JvmField
    var taskPoolSize = Runtime.getRuntime().availableProcessors() + 1

    @JvmField
    var doLoadJars = true

    @JvmField
    var startCommand = ""

    @JvmField
    var enableRemote = false

    @JvmField
    var enableShellCommand = false

    @JvmField
    var accessToken = ""

    @JvmField
    var remotePort = 32768

    @JvmField
    var enableParallel = true

    companion object {
        /**
         * Load the config `cfg.json`
         */
        @JvmStatic
        fun initConfig(): Config {
            var cfg = Config()
            if (!config.exists()) {
                try {
                    Files.newBufferedWriter(config.toPath()).use { writer ->
                        config.createNewFile()
                        val config = Config()
                        config.accessToken = UUID.randomUUID().toString().replace("-", "")
                        GsonBuilder().setPrettyPrinting().create().toJson(config, writer)
                        writer.flush()
                    }
                } catch (e: Exception) {
                    LOGGER.info("Error on loading config {}", e.toString())
                }
            }
            try {
                Files.newBufferedReader(config.toPath())
                    .use { reader -> cfg = Gson().fromJson(reader, Config::class.java) }
            } catch (e: IOException) {
                LOGGER.info("Error on loading config {}", e.toString())
            }
            return cfg
        }
    }
}
