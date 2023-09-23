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

import com.google.common.base.Splitter
import com.google.common.collect.Lists
import net.liyze.basin.util.printException
import org.slf4j.LoggerFactory
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
    val vars: MutableMap<String, String> = ConcurrentHashMap()

    @Volatile
    private var latestResult: Boolean = true

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
    fun parseString(ac: String): Boolean {
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
                    logger.printException(e)
                }
            } else logger.error("Unknown command: $cmdName")
        }
        return false
    }

    fun parse(ac: String): CommandParser {
        latestResult = parseString(ac)
        return this
    }

    fun getLatestResult(): Boolean = latestResult

    companion object {
        /**
         * All Parser.
         */
        @JvmField
        val cs: MutableList<CommandParser> = ArrayList()
    }
}
