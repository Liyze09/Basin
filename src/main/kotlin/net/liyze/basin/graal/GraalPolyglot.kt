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

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.io.Files
import net.liyze.basin.BasinFramework
import net.liyze.basin.graal.exception.UnsupportedLanguageException
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.Value
import java.io.File

internal class GraalPolyglot : Polyglot {
    private val context: Context = Context.newBuilder()
        .allowAllAccess(true)
        .build()
    private val languageMap: BiMap<String, String> = HashBiMap.create(
        mapOf(
            "rb" to "ruby",
            "py" to "python",
            "js" to "js",
            "r" to "R",
            "bc" to "llvm"
        )
    )
    init {
        arrayOf("js", "python", "ruby", "R", "llvm").forEach {
            val binding: Value
            try {
                binding = context.getBindings(it)
            } catch (_: IllegalArgumentException) {
                languageMap.inverse().remove(it)
                return@forEach
            } catch (_: Throwable) {
                return@forEach
            }
            try {
                binding.putMember("basin", BasinFramework)
            } catch (_: Exception) {
            }
        }
    }

    override fun loadScript(path: String) {
        val language: String = languageMap[Files.getFileExtension(path).lowercase()]
            ?: throw UnsupportedLanguageException(path)
        context.eval(
            Source.newBuilder(
                language,
                File(path.replace('/', File.separatorChar))
            ).build()
        )
    }
}