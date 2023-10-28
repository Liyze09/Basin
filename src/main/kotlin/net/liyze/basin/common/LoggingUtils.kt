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
@file: JvmName("LoggingUtils")

package net.liyze.basin.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.PrintWriter
import java.io.StringWriter

private val DEFAULT = LoggerFactory.getLogger("EXCEPTION")

@JvmOverloads
fun Logger.printException(throwable: Throwable, level: Level = Level.ERROR) {
    val out = StringWriter()
    val buffer = PrintWriter(out)
    buffer.println()
    throwable.printStackTrace(buffer)
    atLevel(level).log(out.toString())
}

fun Throwable.printException() {
    DEFAULT.printException(this)
}
