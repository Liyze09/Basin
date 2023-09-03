package net.liyze.basin.core.scan

import net.liyze.basin.core.Command
import net.liyze.basin.core.LOGGER
import net.liyze.basin.core.cfg
import java.io.IOException


class ShellCommand : Command {
    override fun run(args: List<String?>) {
        if (!cfg.enableShellCommand) return
        try {
            LOGGER.info(Runtime.getRuntime().exec(args.toTypedArray<String?>()).toString())
        } catch (e: IOException) {
            LOGGER.error(e.toString())
        }
    }

    override fun Name(): String {
        return "exec"
    }
}
