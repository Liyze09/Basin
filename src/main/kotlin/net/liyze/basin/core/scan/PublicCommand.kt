package net.liyze.basin.core.scan


import net.liyze.basin.context.annotation.Component
import net.liyze.basin.core.Command
import net.liyze.basin.core.CommandParser
import net.liyze.basin.core.publicVars

@Component
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
