/*
 * Copyright (c) 2023 Liyze09
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liyze.basin.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.liyze.basin.util.printException
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
    var startCommand = ""

    @JvmField
    var enableRemote = false

    @JvmField
    var enableShellCommand = false

    @JvmField
    var accessToken = ""

    @JvmField
    var remotePort = 32768

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
                    LOGGER.printException(e)
                }
            }
            try {
                Files.newBufferedReader(config.toPath())
                    .use { reader ->
                        cfg = Gson().fromJson(reader, Config::class.java)
                    }
            } catch (e: IOException) {
                LOGGER.printException(e)
            }
            return cfg
        }
    }
}
