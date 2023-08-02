package net.liyze.basin.core.scan;

import com.itranswarp.summer.annotation.Component;
import net.liyze.basin.core.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.liyze.basin.core.Basin.app;

@Component
public class RpcStartCommand implements Command {
    @Override
    public void run(@NotNull List<String> args) {
        app.startRpcService(Integer.parseInt(args.get(0)));
    }

    @Override
    public @NotNull String Name() {
        return "rpc";
    }
}
