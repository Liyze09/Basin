@file: JvmName("LoggingUtils")

package net.liyze.basin.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.PrintWriter
import java.io.StringWriter

private val DEFAULT = LoggerFactory.getLogger("EXCEPTION")

@JvmOverloads
fun Logger.printException(throwable: Throwable, level: Level? = Level.ERROR) {
    val out = StringWriter()
    val buffer = PrintWriter(out)
    throwable.printStackTrace(buffer)
    atLevel(level).log(out.toString())
}

fun printException(throwable: Throwable) {
    DEFAULT.printException(throwable)
}
