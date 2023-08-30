package com.itranswarp.summer.io

import java.io.IOException
import java.io.InputStream

@FunctionalInterface
fun interface InputStreamCallback<T> {
    @Throws(IOException::class)
    fun doWithInputStream(stream: InputStream?): T
}
