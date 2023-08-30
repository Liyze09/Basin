package net.liyze.basin.core.scan

import net.liyze.basin.context.annotation.Component
import net.liyze.basin.core.Command
import net.liyze.basin.core.shutdown

/**
 * Stop basin after all task finished.
 *
 * @author Liyze09
 */
@Component
class StopCommand : Command {
    override fun run(args: List<String?>) {
        shutdown()
    }

    override fun Name(): String {
        return "stop"
    }
}
