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

package net.liyze.basin.graal


import net.liyze.basin.BasinFramework
import net.liyze.basin.graal.exception.UnsupportedLanguageException
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source
import java.io.File

internal class GraalPolyglot : Polyglot {
    private val context: Context = Context.newBuilder()
        .allowAllAccess(true)
        .build()

    init {
        arrayOf("js", "python", "ruby", "R", "llvm").forEach {
            try {
                context.getBindings(it).putMember("basin", BasinFramework)
            } catch (_: Exception) {
            }
        }



    }

    override fun loadScript(path: String) {
        val language = when {
            path.endsWith("rb") -> "ruby"
            path.endsWith("py") -> "python"
            path.endsWith("js") -> "js"
            path.endsWith("R", true) -> "R"
            path.endsWith("bc") -> "llvm"
            else -> throw UnsupportedLanguageException(path)
        }
        context.eval(
            Source.newBuilder(
                language,
                File(path.replace('/', File.separatorChar))
            ).build()
        )
    }
}