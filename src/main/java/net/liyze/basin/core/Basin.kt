@file:JvmName("Basin")
@file:Suppress("unused")

package net.liyze.basin.core

import com.google.common.base.Splitter
import com.itranswarp.summer.AnnotationConfigApplicationContext
import com.itranswarp.summer.ApplicationContext
import com.itranswarp.summer.BeanDefinition
import com.itranswarp.summer.annotation.ComponentScan
import com.moandjiezana.toml.Toml
import net.liyze.basin.core.Config.Companion.initConfig
import net.liyze.basin.core.remote.RemoteServer
import net.liyze.basin.core.scan.ServerCommand
import org.jetbrains.annotations.Contract
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.jar.JarFile
import kotlin.system.exitProcess

/**
 * Basin start class
 */
@ComponentScan(value = ["net.liyze.basin.core.scan", "net.liyze.basin.rpc"])
class BasinApplication private constructor()

@JvmField
val LOGGER: Logger = LoggerFactory.getLogger("Basin")

@JvmField
val commands = HashMap<String, Command?>()

@JvmField
val userHome = File("data" + File.separator + "home")
val config = File("data" + File.separator + "cfg.json")
val script = File("data" + File.separator + "script")
val BootClasses: MutableList<Class<*>> = ArrayList()
val CONSOLE_COMMAND_PARSER = CommandParser()

@JvmField
val publicVars: MutableMap<String, String> = ConcurrentHashMap()

@JvmField
val contexts: List<ApplicationContext> = ArrayList()
val jars = File("data" + File.separator + "jars")
var env = Toml()

@JvmField
var servicePool: ExecutorService = Executors.newCachedThreadPool()
var taskPool: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)

@JvmField
var envMap: MutableMap<String, Any> = HashMap()

@JvmField
var cfg = initConfig()

@JvmField
var app: ApplicationContext? = null
private var command: String? = null

/**
 * Basin's ASCII banner
 */
@Suppress("SpellCheckingInspection")
var banner = String.format(
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
                    :: Basin :: (%s)
                    
                    """.trimIndent(), version
)
lateinit var startArgs: Array<String>

fun main(args: Array<String>) {
    startArgs = args
    start()
}

fun start() {
    LOGGER.info("----------------------------------------------\nBasin started.")
    taskPool.submit(Thread {
        try {
            userHome.mkdirs()
            script.mkdirs()
            jars.mkdirs()
            loadEnv()
            envMap.forEach { (key: String, value: Any) -> publicVars[key] = value.toString() }
            LOGGER.info("Init method are finished.")
            if (cfg.doLoadJars) {
                loadJars()
                LOGGER.info("Loader's method are finished.")
                BootClasses.forEach(Consumer { i: Class<*> ->
                    servicePool.submit(Thread {
                        try {
                            val `in` = i.getDeclaredConstructor().newInstance() as BasinBoot
                            `in`.afterStart()
                        } catch (e: Exception) {
                            LOGGER.error(e.message)
                        }
                    })
                })
                LOGGER.info("Startup method are finished.")
                app = AnnotationConfigApplicationContext(BasinApplication::class.java)
                (app as AnnotationConfigApplicationContext).findBeanDefinitions(Command::class.java)
                    .forEach(Consumer { def: BeanDefinition -> registerCommand(def.instance as Command) })
                if (cfg.startCommand.isNotBlank()) CONSOLE_COMMAND_PARSER.parse(cfg.startCommand)
                if (cfg.enableRemote && cfg.accessToken.isNotBlank()) {
                    try {
                        RemoteServer(cfg.accessToken, cfg.remotePort, CommandParser()).start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    })
    try {
        Thread {
            Scanner(System.`in`).use { scanner ->
                while (true) {
                    command = scanner.nextLine()
                    if (cfg.enableParallel) {
                        taskPool.submit(Thread {
                            try {
                                CONSOLE_COMMAND_PARSER.sync().parse(command!!)
                            } catch (e: Exception) {
                                LOGGER.error(e.toString())
                            }
                        })
                    } else {
                        try {
                            CONSOLE_COMMAND_PARSER.sync().parse(command!!)
                        } catch (e: Exception) {
                            LOGGER.error(e.toString())
                        }
                    }
                }
            }
        }.start()
    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }
    println(banner)
}

@Throws(IOException::class)
fun loadEnv() {
    val envFile = File("data" + File.separator + "env.toml")
    if (!envFile.exists()) {
        try {
            envFile.createNewFile()
        } catch (e: IOException) {
            LOGGER.error("Error when create environment variable file: ", e)
        }
        FileWriter(envFile).use { writer -> writer.write("# Basin Environment Variables") }
    }
    envMap.putAll(env.read(envFile).toMap())
    taskPool = Executors.newFixedThreadPool(cfg.taskPoolSize)
}

@Throws(Exception::class)
fun loadJars() {
    val children = jars.listFiles { _: File?, s: String -> s.matches(".*\\.jar".toRegex()) }
    var b: String
    var c: String
    var bl: Iterable<String?>
    var cl: Iterable<String?>
    if (children == null) {
        LOGGER.error("Jars file isn't exist!")
    } else {
        for (jar in children) {
            JarFile(jar).use { jarFile ->
                b = jarFile.manifest.mainAttributes.getValue("Boot-Class")
                val sp = Splitter.on(" ").trimResults()
                bl = sp.split(b)
                c = jarFile.manifest.mainAttributes.getValue("Export-Command")
                cl = sp.split(c)
                for (i in bl) {
                    if (b.isNotBlank()) {
                        val cls = Class.forName(i)
                        val boot = cls.getDeclaredConstructor().newInstance()
                        if (boot is BasinBoot) {
                            Thread { boot.onStart() }.start()
                            BootClasses.add(cls)
                        } else {
                            LOGGER.warn("App-BootClass {} is unsupported", jar.getName())
                        }
                    }
                }
                for (i in cl) {
                    if (c.isNotBlank()) {
                        val cls = Class.forName(i)
                        val command = cls.getDeclaredConstructor().newInstance()
                        if (command is Command) {
                            registerCommand(command)
                        } else {
                            LOGGER.warn("Command-Class {} is unsupported", jar.getName())
                        }
                    }
                }
            }
        }
    }
}

fun registerCommand(cmd: Command) {
    commands[cmd.Name()] = cmd
}

@get:Contract(pure = true)
val version: String
    /**
     * Get version version's String.
     */
    get() = "1.6"
val versionNum: Int
    /**
     * Get version version's int.
     */
    get() = 6

/**
 * Stop basin after all task finished.
 */

fun shutdown() {
    Thread {
        LOGGER.info("Stopping\n")
        BootClasses.forEach(Consumer { i: Class<*> ->
            try {
                (i.getDeclaredConstructor().newInstance() as BasinBoot).beforeStop()
            } catch (ignored: Exception) {
            }
        })
        taskPool.shutdown()
        servicePool.shutdownNow()
        try {
            taskPool.awaitTermination(4, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            exitProcess(0)
        }
        exitProcess(0)
    }.start()
}

/**
 * Restart basin.
 */
fun restart() {
    Thread {
        BootClasses.forEach(Consumer { i: Class<*> ->
            try {
                val `in` = i.getDeclaredConstructor().newInstance() as BasinBoot
                `in`.beforeStop()
            } catch (e: Exception) {
                LOGGER.error(e.toString())
            }
        })
        CommandParser.cs.forEach(Consumer { c: CommandParser -> c.vars.clear() })
        taskPool.shutdownNow()
        servicePool.shutdownNow()
        RemoteServer.servers.forEach(Consumer { obj: Server -> obj.stop() })
        RemoteServer.servers.clear()
        ServerCommand.serverMap.values.forEach(Consumer { obj: Server -> obj.stop() })
        ServerCommand.serverMap.clear()
        commands.clear()
        BootClasses.clear()
        publicVars.clear()
        try {
            taskPool.awaitTermination(3, TimeUnit.SECONDS)
        } catch (ignore: InterruptedException) {
        }
        taskPool = Executors.newFixedThreadPool(cfg.taskPoolSize)
        servicePool = Executors.newCachedThreadPool()
        app!!.close()
        app = AnnotationConfigApplicationContext(BasinApplication::class.java)
        (app as AnnotationConfigApplicationContext).findBeanDefinitions(Command::class.java)
            .forEach(Consumer { def: BeanDefinition -> registerCommand(def.instance as Command) })
        try {
            loadEnv()
        } catch (e: Exception) {
            LOGGER.error(e.toString())
        }
        try {
            loadJars()
            BootClasses.forEach(Consumer { i: Class<*> ->
                try {
                    val `in` = i.getDeclaredConstructor().newInstance() as BasinBoot
                    `in`.afterStart()
                } catch (e: Exception) {
                    LOGGER.error(e.toString())
                }
            })
        } catch (e: Exception) {
            LOGGER.error(e.toString())
        }
        if (cfg.startCommand.isNotBlank()) CONSOLE_COMMAND_PARSER.parse(cfg.startCommand)
        if (cfg.enableRemote && cfg.accessToken.isNotBlank()) {
            try {
                RemoteServer(cfg.accessToken, cfg.remotePort, CommandParser()).start()
            } catch (e: Exception) {
                LOGGER.error(e.toString())
            }
        }
        LOGGER.info("Restarted!")
    }.start()
}
