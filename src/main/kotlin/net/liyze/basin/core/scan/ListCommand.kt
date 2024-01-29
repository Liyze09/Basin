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

package net.liyze.basin.core.scan

import net.liyze.basin.Article
import net.liyze.basin.core.Command
import net.liyze.basin.core.commands
import net.liyze.basin.core.envMap
import org.slf4j.Logger
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Print all command loaded.
 *
 * @author Liyze09
 */

class ListCommand : Command {
    override fun run(args: List<String?>, logger: Logger, context: Article) {
        val writer = StringWriter()
        val print = PrintWriter(writer)
        print.println()
        print.println("Commands")
        for (i in commands.keys) {
            print.print(" ")
            print.println(i)
        }
        print.println("Variables")
        for ((key, value) in envMap.entries) {
            print.print(" $key = ")
            print.println(value)
        }
        logger.info(writer.toString())
    }

    override fun getName(): String {
        return "list"
    }
}
