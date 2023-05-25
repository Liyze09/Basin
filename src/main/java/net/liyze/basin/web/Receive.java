package net.liyze.basin.web;

import net.liyze.basin.core.Main;

import java.io.IOException;
import java.io.InputStream;

public class Receive {
    private final InputStream input;

    public Receive(InputStream input) {
        this.input = input;
    }

    public String get() {
        StringBuilder str = new StringBuilder(2048);
        int bytes;
        byte[] data = new byte[2048];
        try {
            bytes = input.read(data);
        } catch (IOException e) {
            bytes = -1;
            Main.LOGGER.error("HTTP Error: {}", e.toString());
        }
        for (int i = 0; i < bytes; ++i) {
            str.append((char) data[i]);
        }
        int a, b;
        String c = str.toString();
        a = c.indexOf(' ');
        if (a != -1) {
            b = c.indexOf(' ', a + 1);
            if (b > a) {
                return c.substring(a + 1, b);
            }
        }
        return null;
    }
}
