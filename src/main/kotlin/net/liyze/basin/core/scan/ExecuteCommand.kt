package net.liyze.basin.core.scan


import net.liyze.basin.context.annotation.Component
import net.liyze.basin.core.Command
import net.liyze.basin.core.CommandParser
import net.liyze.basin.core.servicePool

/**
 * Put command into a CachedThreadPool
 * /execute command args...
 *
 * @author Liyze09
 */
@Component
class ExecuteCommand : Command {
    override fun run(args: List<String?>) {
        servicePool.submit(Thread { CommandParser().sync().parse(args.requireNoNulls()) })
    }

    override fun Name(): String {
        return "execute"
    }
}
