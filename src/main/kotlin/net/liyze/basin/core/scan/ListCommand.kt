package net.liyze.basin.core.scan

import net.liyze.basin.context.annotation.Component
import net.liyze.basin.core.Command
import net.liyze.basin.core.LOGGER
import net.liyze.basin.core.commands
import net.liyze.basin.core.envMap

/**
 * Print all command loaded.
 *
 * @author Liyze09
 */
@Component
class ListCommand : Command {
    override fun run(args: List<String?>) {
        LOGGER.info("Commands")
        for (i in commands.keys) {
            println(i)
        }
        LOGGER.info("Variables")
        for ((key, value) in envMap.entries) {
            print("$key = ")
            println(value)
        }
    }

    override fun Name(): String {
        return "list"
    }
}
