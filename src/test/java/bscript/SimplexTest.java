package bscript;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class SimplexTest {

    @Test
    void init() throws IOException {
        new Simplex().init("data/output/pom".replace('/', File.separatorChar));
    }
}