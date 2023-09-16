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
@file: JvmName("BasinFramework")

package net.liyze.basin

import net.liyze.basin.core.start
import net.liyze.basin.event.EventBus
import net.liyze.basin.http.HttpServer
import net.liyze.basin.rpc.RpcService

val httpServer = HttpServer
val rpcServer = RpcService
val eventBus = EventBus
fun startBasin() {
    start()
}