package net.liyze.basin.core.scan

import net.liyze.basin.core.Command
import net.liyze.basin.http.HttpServer


class ServerCommand : Command {
    override fun run(args: List<String?>) {
        if (args.isNotEmpty()) HttpServer.stop()
        else HttpServer.start()
    }

    override fun Name(): String {
        return "server"
    }
}
