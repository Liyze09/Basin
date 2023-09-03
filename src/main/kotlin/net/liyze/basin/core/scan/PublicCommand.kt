package net.liyze.basin.core.scan

import net.liyze.basin.core.Command
import net.liyze.basin.core.CommandParser
import net.liyze.basin.core.publicVars

class PublicCommand : Command {
    override fun run(args: List<String?>) {
        val parser = CommandParser()
        parser.sync().parse(args.requireNoNulls())
        publicVars.putAll(parser.vars)
    }

    override fun Name(): String {
        return "public"
    }
}
