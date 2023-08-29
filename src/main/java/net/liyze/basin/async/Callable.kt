package net.liyze.basin.async

@FunctionalInterface
interface Callable<I, T> {
    infix fun run(input: I): T
}