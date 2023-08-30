package net.liyze.basin.core.scan

import net.liyze.basin.context.annotation.Component
import net.liyze.basin.core.Command
import kotlin.system.exitProcess

@Component
class ForceStopCommand : Command {
    override fun run(args: List<String?>) {
        exitProcess(0)
    }

    @Suppress("SpellCheckingInspection")
    override fun Name(): String {
        return "forcestop"
    }
}
