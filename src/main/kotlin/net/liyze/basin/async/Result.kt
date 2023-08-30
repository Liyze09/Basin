package net.liyze.basin.async

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
open class Result<I, T>(
    open val action: Callable<I, T>,
    val input: I,
) {

    @Volatile
    var result: T? = null

    init {
        GlobalScope.launch(Dispatchers.IO) {
            result = run()
        }
    }

    protected open fun run(): T {
        return action run input
    }

    open fun await(): T {
        while (result == null) {
            Thread.onSpinWait()
        }
        return result!!
    }
}