package net.liyze.basin.core.scan;

import com.itranswarp.summer.annotation.Component;
import net.liyze.basin.core.Command;
import net.liyze.basin.rpc.RpcClient;
import net.liyze.basin.rpc.annotation.RpcService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

@Component
public class RpcRequestCommand implements Command {
    @RpcService
    public StringBuffer get(String s) {
        return new StringBuffer(s);
    }

    @Override
    public void run(@NotNull List<String> args) {
        try {
            var client = RpcClient.getRpcClient(args.get(0),
                    Integer.parseInt(args.get(1)));
            int sign = client.request("net.liyze.basin.core.scan", "get", new String[]{"oops"});
            System.out.println(client.get(sign, 114514));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull String Name() {
        return "ping";
    }
}
