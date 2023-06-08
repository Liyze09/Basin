package net.liyze.basin.http;

import org.smartboot.http.server.HttpRequest;

@FunctionalInterface
public interface View {
    /**
     * Returns data as ByteArray.
     */
    byte[] getPage(HttpRequest request);
}
