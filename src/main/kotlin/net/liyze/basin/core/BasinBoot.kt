package net.liyze.basin.core

/**
 * Basin's Plugin/Boot SPI
 */
interface BasinBoot {
    /**
     * Runs after basin starting
     */
    fun afterStart() {}

    /**
     * Runs before basin stopping
     */
    fun beforeStop() {}
}
