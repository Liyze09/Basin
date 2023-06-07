package net.liyze.basin.http;

import org.smartboot.http.server.HttpRequest;

@FunctionalInterface
public interface View {
    byte[] getPage(HttpRequest request);
}
