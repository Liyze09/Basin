package net.liyze.basin.http

import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse

@FunctionalInterface
fun interface HttpHandler {
    fun handle(job: HttpJob): ModelAndView
}

data class HttpJob(val request: HttpRequest, val response: HttpResponse)