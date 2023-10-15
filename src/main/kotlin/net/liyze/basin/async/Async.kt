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

@file:JvmName("Async")
@file:Suppress("NOTHING_TO_INLINE")

package net.liyze.basin.async

inline fun <I, T> async(args: I, action: Callable<I, T>): Result<I, T> = Result(action, args)

inline fun <T> async(action: SingleCallable<T>): Result<Any, T> = SingleResult(action)

inline fun <T> await(future: Result<*, T>): T = future.await()


class SingleResult<T>(
    override val action: SingleCallable<T>
) : Result<Any, T>(action, Any()) {
    override fun run(): T {
        return action.run()
    }
}

@FunctionalInterface
fun interface SingleCallable<T> : Callable<Any, T> {
    override fun run(input: Any): T {
        throw UnsupportedOperationException()
    }

    fun run(): T
}