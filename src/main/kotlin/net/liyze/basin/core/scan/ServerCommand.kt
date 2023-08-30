package net.liyze.basin.core.scan

import net.liyze.basin.context.annotation.Component
import net.liyze.basin.core.Command
import net.liyze.basin.core.LOGGER
import net.liyze.basin.core.Server
import net.liyze.basin.http.HttpServer
import java.util.concurrent.ConcurrentHashMap

@Component
class ServerCommand : Command {
    override fun run(args: List<String?>) {
        val name = args[0]!!
        if (args[1] != "stop") {
            serverMap[name] = HttpServer(name, args[1]!!.toInt()).start()
        } else {
            val server = serverMap.remove(name)
            if (server != null) server.stop() else LOGGER.error("Server {} was not exist.", name)
        }
    }

    override fun Name(): String {
        return "server"
    }

    companion object {
        val serverMap: ConcurrentHashMap<String, Server> = ConcurrentHashMap<String, Server>()
    }
}
