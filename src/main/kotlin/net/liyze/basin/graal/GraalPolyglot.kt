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

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source
import java.io.FileNotFoundException


internal class GraalPolyglot : Polyglot {
    private val context: Context = Context.newBuilder()
        .allowAllAccess(true)
        .option("js.strict", "true")
        .build()

    init {
        arrayOf("module", "java", "fs").forEach {
            GraalPolyglot::class.java.getResourceAsStream("/std/$it.js")?.reader()?.use { reader ->
                context.eval(
                    Source.newBuilder("js", reader, it)
                        .mimeType("application/javascript")
                        .build()
                )
            }
        }
    }

    override fun loadScript(path: String) {
        context.eval(
            Source.newBuilder(
                "js",
                GraalPolyglot::class.java.getResource(path) ?: throw FileNotFoundException(path)
            ).build()
        )
    }
}