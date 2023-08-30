package net.liyze.basin.core.scan

import net.liyze.basin.context.annotation.Component
import net.liyze.basin.core.Command
import net.liyze.basin.core.restart

@Component
class RestartCommand : Command {
    override fun run(args: List<String?>) {
        restart()
    }

    override fun Name(): String {
        return "restart"
    }
}
