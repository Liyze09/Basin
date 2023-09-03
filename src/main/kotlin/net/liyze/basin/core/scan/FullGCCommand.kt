package net.liyze.basin.core.scan

import net.liyze.basin.core.Command
import net.liyze.basin.core.LOGGER


class FullGCCommand : Command {
    override fun run(args: List<String?>) {
        System.gc()
        LOGGER.info("Full GC")
    }

    override fun Name(): String {
        return "fgc"
    }
}
