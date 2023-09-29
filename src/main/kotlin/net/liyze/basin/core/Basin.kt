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

import net.liyze.basin.core.remote.RemoteServer
import net.liyze.basin.core.scan.*
import net.liyze.basin.http.HttpServer
import net.liyze.basin.util.printException
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
val LOGGER: Logger = LoggerFactory.getLogger("Basin")

@JvmField
val commands = HashMap<String, Command>()
val bootClasses: MutableList<Class<*>> = ArrayList()
val CONSOLE_COMMAND_PARSER = CommandParser()

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
var banner =
    """
                    ${'\r'}
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
    LOGGER.info("----------------------------------------------\nBasin started.")
    Thread.ofVirtual().start {
        try {
            loadEnv()
            envMap.forEach { (key: String, value: Any) -> publicVars[key] = value }
            if (cfg.startCommand.isNotBlank()) CONSOLE_COMMAND_PARSER.parseString(cfg.startCommand)
            if (cfg.enableRemote && cfg.accessToken.isNotBlank()) {
                try {
                    RemoteServer(cfg.accessToken, cfg.remotePort, CommandParser()).start()
                } catch (e: Exception) {
                    LOGGER.printException(e)
                }
            }
        } catch (e: Exception) {
            LOGGER.printException(e)
        }
    }
    try {
        registerListOfCommand(
            listOf(
                ForceStopCommand(),
                FullGCCommand(),
                ListCommand(),
                PublicCommand(),
                RemoteCommand(),
                RestartCommand(),
                ServerCommand(),
                ShellCommand(),
                StopCommand(),
            )
        )
        Thread.ofVirtual().start {
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
                (it.getDeclaredConstructor().newInstance() as BasinBoot).afterStart()
            }
        })
    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }
    println(banner)
}

fun loadEnv() {
    val envFile = File("data" + File.separator + "env.properties")
    val props = Properties()
    if (envFile.exists()) {
        props.load(FileReader(envFile))
    }
    envMap.putAll(System.getenv())
    val names = props.stringPropertyNames()
    for (name in names) {
        envMap[name] = props.getProperty(name)
    }
}

fun registerCommand(cmd: Command) {
    commands[cmd.Name()] = cmd
}

fun registerListOfCommand(list: List<Command>) {
    list.forEach {
        commands[it.Name()] = it
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
        bootClasses.forEach(Consumer { i: Class<*> ->
            try {
                (i.getDeclaredConstructor().newInstance() as BasinBoot).beforeStop()
            } catch (e: Exception) {
                LOGGER.printException(e)
            }
        })
        exitProcess(0)
    }
}

/**
 * Restart basin.
 */
fun restart() {
    Thread.ofVirtual().start {
        bootClasses.forEach(Consumer { i: Class<*> ->
            try {
                val `in` = i.getDeclaredConstructor().newInstance() as BasinBoot
                `in`.beforeStop()
            } catch (e: Exception) {
                LOGGER.printException(e)
            }
        })
        CommandParser.cs.forEach(Consumer { c: CommandParser -> c.vars.clear() })
        RemoteServer.servers.forEach(Consumer { obj: Server -> obj.stop() })
        RemoteServer.servers.clear()
        HttpServer.stop()
        HttpServer.start()
        commands.clear()
        publicVars.clear()
        loadEnv()
        bootClasses.forEach(Consumer {
            try {
                (it.getDeclaredConstructor().newInstance() as BasinBoot).afterStart()
            } catch (e: Exception) {
                LOGGER.printException(e)
            }
        })
        if (cfg.startCommand.isNotBlank()) CONSOLE_COMMAND_PARSER.parseString(cfg.startCommand)
        if (cfg.enableRemote && cfg.accessToken.isNotBlank()) {
            try {
                RemoteServer(cfg.accessToken, cfg.remotePort, CommandParser()).start()
            } catch (e: Exception) {
                LOGGER.printException(e)
            }
        }
        LOGGER.info("Restarted!")
    }.start()
}
