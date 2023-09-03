package net.liyze.basin.core.scan

import net.liyze.basin.core.Command
import net.liyze.basin.core.restart


class RestartCommand : Command {
    override fun run(args: List<String?>) {
        restart()
    }

    override fun Name(): String {
        return "restart"
    }
}
