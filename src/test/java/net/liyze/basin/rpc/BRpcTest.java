package net.liyze.basin.rpc;

import com.itranswarp.summer.AnnotationConfigApplicationContext;
import com.itranswarp.summer.annotation.Component;
import net.liyze.basin.core.Basin;
import net.liyze.basin.rpc.annotation.RpcService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static net.liyze.basin.core.Basin.app;

@Component
class BRpcTest {
    @RpcService
    public @NotNull StringBuilder test(String s) {
        return new StringBuilder(s);
    }

    @Disabled
    @Test
    void startRpcServer() throws InterruptedException {
        app = new AnnotationConfigApplicationContext(Basin.class);
        BRpcStarter.startRpcServer(8000);
        Thread.sleep(500);
        var client = new HttpRpcClient("http://localhost:8000/");
        System.out.println(client.getResult(client.request("bRpcTest", "test", new String[]{"oops"})));
    }
}