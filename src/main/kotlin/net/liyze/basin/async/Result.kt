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

package net.liyze.basin.async

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
open class Result<I, T>(
    protected open val action: Callable<I, T>,
    protected val input: I,
) {

    @Volatile
    protected var result: T? = null

    init {
        GlobalScope.launch(Dispatchers.Default) {
            result = run()
        }
    }

    protected open suspend fun run(): T {
        return action run input
    }

    fun await(): T {
        while (result == null) {
            Thread.onSpinWait()
        }
        return result!!
    }
}