package net.liyze.basin.core.scan

import net.liyze.basin.context.annotation.Component
import net.liyze.basin.core.Command
import net.liyze.basin.core.envMap
import net.liyze.basin.core.remote.LOGGER
import net.liyze.basin.core.remote.send

@Component
class RemoteCommand : Command {
    override fun run(args: List<String?>) {
        val host: String = args[0]!!
        try {
            send(
                java.lang.String.join(" ", args.subList(1, args.size)),
                host,
                envMap["\"" + host + "_token\""].toString()
            )
        } catch (e: Exception) {
            LOGGER.error(e.toString())
        }
    }

    override fun Name(): String {
        return "remote"
    }
}
