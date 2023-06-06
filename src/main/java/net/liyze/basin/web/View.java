package net.liyze.basin.web;

import org.smartboot.http.server.HttpRequest;

@FunctionalInterface
public interface View {
    byte[] getPage(HttpRequest request);
}
