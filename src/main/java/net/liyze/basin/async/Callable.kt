package net.liyze.basin.async

@FunctionalInterface
fun interface Callable<I, T> {
    infix fun run(input: I): T
}