package net.liyze.basin.core

/**
 * Basin Command's SPI
 */
interface Command {
    /**
     * What to do when start the command
     *
     * @author Liyze09
     */
    fun run(args: List<String?>)

    /**
     * The Name of the command
     */
    fun Name(): String
}
