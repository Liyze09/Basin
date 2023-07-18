package bscript;

import bscript.exception.LoadFailedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class BScriptExecutableFile {

    @SuppressWarnings("resource")
    public static void main(String[] args) throws IOException {
        var runtime = new BScriptRuntime();
        try (InputStream stream = BScriptExecutableFile.class.getResourceAsStream("/beans")) {
            if (stream != null) {
                new BufferedReader(new StringReader(new String(stream.readAllBytes(), StandardCharsets.UTF_8)))
                        .lines()
                        .forEach(line -> {
                            try {
                                runtime.load(Class.forName(line));
                            } catch (ClassNotFoundException e) {
                                throw new LoadFailedException(e);
                            }
                        });
                runtime.run();
            } else {
                throw new LoadFailedException();
            }
        }
    }
}
