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

import net.liyze.basin.common.printException
import net.liyze.basin.core.Command
import net.liyze.basin.core.envMap
import net.liyze.basin.core.remote.send
import org.slf4j.Logger


class RemoteCommand : Command {
    override fun run(args: List<String?>, logger: Logger) {
        val host: String = args[0]!!
        try {
            envMap["\"" + host + "_token\""]?.let {
                send(
                    java.lang.String.join(" ", args.subList(1, args.size)),
                    host,
                    it
                )
            } ?: throw RuntimeException("Remote token not found: $host")
        } catch (e: Exception) {
            e.printException()
        }
    }

    override fun getName(): String {
        return "remote"
    }
}
