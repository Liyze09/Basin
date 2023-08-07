package net.liyze.basin.rpc;

import net.liyze.basin.core.Server;

public class BRpcStarter {
    private BRpcStarter() {
        throw new UnsupportedOperationException();
    }

    private static Server server = null;

    public static void startRpcServer(int port) {
        if (server == null) {
            server = new RpcServer(port).start();
        }
    }
}
