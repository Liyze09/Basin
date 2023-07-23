package bscript;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;


class SimplexTest {
    @Disabled
    @Test
    void init() throws IOException {
        var s = new Simplex();
        s.init();
        s.load();
    }
}