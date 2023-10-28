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

import net.liyze.basin.core.Command
import net.liyze.basin.core.CommandParser
import net.liyze.basin.core.publicVars

class PublicCommand : Command {
    override fun run(args: List<String?>) {
        val parser = CommandParser()
        parser.sync().parse(args.requireNoNulls())
        publicVars.putAll(parser.vars)
    }

    override fun Name(): String {
        return "public"
    }
}
