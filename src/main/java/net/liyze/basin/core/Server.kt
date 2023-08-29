package net.liyze.basin.core

/**
 * Basin AIO server SPI
 */
interface Server {
    /**
     * Stop the server.
     */
    fun stop()

    /**
     * Start the server.
     */
    fun start(): Server?
}
