@file:JvmName("Async")

package net.liyze.basin.async

fun <I, T> async(args: I, action: Callable<I, T>): Result<I, T> {
    return Result(action, args)
}

fun <T> async(action: SingleCallable<T>): Result<Any, T> {
    return SingleResult(action)
}

class SingleResult<T>(
    override val action: SingleCallable<T>
) : Result<Any, T>(action, Any()) {
    override fun run(): T {
        return action.run()
    }
}

@FunctionalInterface
interface SingleCallable<T> : Callable<Any, T> {
    override fun run(input: Any): T {
        throw UnsupportedOperationException()
    }

    fun run(): T
}