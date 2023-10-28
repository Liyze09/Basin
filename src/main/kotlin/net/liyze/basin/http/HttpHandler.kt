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

package net.liyze.basin.http

import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse

@FunctionalInterface
fun interface HttpHandler {
    fun handle(job: HttpJob): ModelAndView
}

@FunctionalInterface
fun interface HttpFilter {
    fun filter(job: HttpJob): Status
}

data class HttpJob(val request: HttpRequest, val response: HttpResponse)

enum class Status {
    SKIP_ALL,
    SKIP_FILTER,
    PASS
}