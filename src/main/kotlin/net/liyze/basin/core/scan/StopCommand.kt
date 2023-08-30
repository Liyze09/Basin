package net.liyze.basin.core.scan;

import com.itranswarp.summer.annotation.Component;
import net.liyze.basin.core.Basin;
import net.liyze.basin.core.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Stop basin after all task finished.
 *
 * @author Liyze09
 */
@Component
public class StopCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        Basin.shutdown();
    }

    @Override
    public @NotNull String Name() {
        return "stop";
    }
}
