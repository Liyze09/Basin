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
@file:JvmName("Basin")
@file:Suppress("unused")

package net.liyze.basin.core

import net.liyze.basin.Article
import net.liyze.basin.common.printException
import net.liyze.basin.core.scan.*
import org.jetbrains.annotations.Contract
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import kotlin.system.exitProcess

@JvmField
val articles: MutableMap<String, Article> = HashMap(mapOf("default" to Article("default")))
@JvmField
val LOGGER: Logger = LoggerFactory.getLogger("Basin")

@JvmField
val commands = HashMap<String, Command>()
val bootClasses: MutableList<BasinBoot> = ArrayList()
var CONSOLE_COMMAND_PARSER =
    CommandParser(articles[Config.defaultArticle] ?: throw RuntimeException("Didn't find default Article"))

@JvmField
val publicVars: MutableMap<String, String> = ConcurrentHashMap()

@JvmField
var envMap: MutableMap<String, String> = HashMap()

@JvmField
var cfg = Config

private var command: String? = null

/**
 * Basin's ASCII banner
 */
@Suppress("SpellCheckingInspection")
val banner =
    """
                    BBBBBBBBBBBBBBBBB                                         iiii
                    B::::::::::::::::B                                       i::::i
                    B::::::BBBBBB:::::B                                       iiii
                    BB:::B      B:::::B
                    B::::B       B:::::B   aaaaaaaaaaaaa        ssssssssss   iiiiiii   nnnn  nnnnnnnn
                    B::::B      B:::::B   a::::::::::::a     ss::::::::::s   i:::::i  n:::nn::::::::nn
                    B::::BBBBBB:::::B     aaaaaaaaa:::::a  ss:::::::::::::s   i::::i  n::::::::::::::nn
                    B::::::::::::BB                a::::a  s::::::ssss:::::s  i::::i  nn::::::::::::::n
                    B::::BBBBBB:::::B       aaaaaaa:::::a   s:::::s   ssssss  i::::i   n:::::nnnn:::::n
                    B::::B      B:::::B    aa::::::::::::a     s::::::s       i::::i   n::::n    n::::n
                    B::::B       B:::::B  a::::aaaa::::::a        s::::::s    i::::i   n::::n    n::::n
                    B::::B      B:::::B  a::::a    a:::::a  ssssss   s:::::s  i::::i   n::::n    n::::n
                    BB:::::BBBBBB::::::B a::::a    a:::::a s:::::ssss::::::s i::::::i  n::::n    n::::n
                    B:::::::::::::::::B a:::::aaaa::::::a  s::::::::::::::s  i::::::i  n::::n    n::::n
                    B::::::::::::::::B   a::::::::::aa:::a  s:::::::::::ss   i::::::i  n::::n    n::::n
                    BBBBBBBBBBBBBBBBB     aaaaaaaaaa  aaaa   sssssssssss     iiiiiiii  nnnnnn    nnnnnn
                    :: Basin :: ($version)
                    """.trimIndent()

lateinit var startArgs: Array<String>

fun main(args: Array<String>) {
    startArgs = args
    start()
}

fun start() {
    val t0 = System.currentTimeMillis()
    LOGGER.info("----------------------------------------------\n${banner}")
    try {
        loadEnv()
        envMap.forEach { (key: String, value: Any) -> publicVars[key] = value }
        if (cfg.startCommand.isNotBlank()) CONSOLE_COMMAND_PARSER.parseString(cfg.startCommand)
    } catch (e: Exception) {
        LOGGER.printException(e)
    }
    registerListOfCommand(
        listOf(
            ForceStopCommand(),
            FullGCCommand(),
            ListCommand(),
            PublicCommand(),
            SwitchCommand(),
            RestartCommand(),
            RpcServerCommand(),
            ServerCommand(),
            ShellCommand(),
            StopCommand(),
        )
    )
    Thread.ofPlatform().name("MainLoop").start {
        Scanner(System.`in`).use { scanner ->
            while (true) {
                command = scanner.nextLine()
                Thread.ofVirtual().start {
                    try {
                        CONSOLE_COMMAND_PARSER.sync().parseString(command!!)
                    } catch (e: Exception) {
                        LOGGER.printException(e)
                    }
                }
            }
        }
    }
    bootClasses.forEach(Consumer {
        Thread.ofVirtual().start {
            it.afterStart()
        }
    })
    LOGGER.info("Basin started, took {} ms.", System.currentTimeMillis() - t0)
}

fun loadEnv() {
    val envFile = File("data" + File.separator + "env.properties")
    val props = Properties()
    if (envFile.exists()) {
        props.load(FileReader(envFile))
        LOGGER.info("Loading an environment variable file.")
    }
    envMap.putAll(System.getenv())
    val names = props.stringPropertyNames()
    for (name in names) {
        envMap[name] = props.getProperty(name)
    }
}

fun registerCommand(cmd: Command) {
    commands[cmd.getName()] = cmd
}

fun registerListOfCommand(list: List<Command>) {
    list.forEach {
        commands[it.getName()] = it
    }
}

@get:Contract(pure = true)
val version: String
    /**
     * Get version version's String.
     */
    get() = "3.0"
val versionNum: Int
    /**
     * Get version version's int.
     */
    get() = 7

/**
 * Stop basin.
 */
fun shutdown() {
    Thread.ofVirtual().start {
        LOGGER.info("Stopping\n")
        bootClasses.forEach(Consumer {
            it.beforeStop()
        })
        exitProcess(0)
    }
}

/**
 * Restart basin.
 */
fun restart() {
    Thread.ofVirtual().start {
        bootClasses.forEach(Consumer {
            it.beforeStop()
        })
        CommandParser.cs.forEach(Consumer { c: CommandParser -> c.vars.clear() })
        commands.clear()
        publicVars.clear()
        loadEnv()
        bootClasses.forEach(Consumer {
            it.afterStart()
        })
        if (cfg.startCommand.isNotBlank()) CONSOLE_COMMAND_PARSER.parseString(cfg.startCommand)
        LOGGER.info("Restarted!")
    }.start()
}
