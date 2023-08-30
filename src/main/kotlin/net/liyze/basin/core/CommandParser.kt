package net.liyze.basin.core

import com.google.common.base.Splitter
import com.google.common.collect.Lists
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * Basin Command Parser
 */
@Suppress("unused")
class CommandParser {
    /**
     * This Parser's vars.
     */
    @JvmField
    val vars: MutableMap<String, String> = ConcurrentHashMap()

    init {
        cs.add(this)
    }

    /**
     * Sync variables to the public environment.
     */
    fun sync(): CommandParser {
        vars.putAll(publicVars)
        return this
    }

    /**
     * Parse command from String.
     */
    fun parse(ac: String): Boolean {
        if (ac.isBlank() || ac.startsWith("#")) return true
        LOGGER.info(ac)
        val sp = Splitter.on(" ").trimResults()
        return parse(Lists.newArrayList(sp.split(ac)))
    }

    /**
     * Parse command from a List.
     */
    fun parse(alc: List<String>): Boolean {
        val allArgs: MutableList<MutableList<String?>> = ArrayList()
        run {
            val areaArgs: MutableList<String?> = ArrayList()
            for (i in alc) {
                //Multi Command Apply
                if (i == "&") {
                    allArgs.add(areaArgs)
                    areaArgs.clear()
                    continue
                }
                val s = AtomicReference<String?>(null)
                if (s.get() != null) {
                    areaArgs.add(s.get())
                } else {
                    areaArgs.add(i)
                }
            }
            allArgs.add(areaArgs)
        }
        for (args in allArgs) {
            val cmdName = args[0]
            val logger = LoggerFactory.getLogger(cmdName)
            //Var Define Apply
            if (cmdName!!.matches(".*=.*".toRegex())) {
                val sp = Splitter.on("=").trimResults()
                val v: List<String> = Lists.newArrayList(sp.split(cmdName))
                vars[v[0].trim()] = v[1].trim()
                logger.info(cmdName)
                return true
            }
            args.remove(cmdName)
            val run = commands[cmdName.lowercase(Locale.getDefault()).trim()]
            //Run command method
            if (run != null) {
                try {
                    logger.debug("$cmdName started.")
                    run.run(args)
                    return true
                } catch (e: IndexOutOfBoundsException) {
                    logger.error("Bad arg input.")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else logger.error("Unknown command: $cmdName")
        }
        return false
    }

    @Throws(IOException::class)
    fun parseScript(script: BufferedReader) {
        val lines = script.lines()
        lines.forEach { i: String -> if (!i.isEmpty()) this.parse(i) }
        script.close()
    }

    companion object {
        /**
         * All Parser.
         */
        @JvmField
        val cs: MutableList<CommandParser> = ArrayList()
    }
}
