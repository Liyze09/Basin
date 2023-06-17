package net.liyze.basin.script.test;

import net.liyze.basin.script.exp.BScript;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("ST1")
public final class ScriptTest{
    @Test
    public void scriptTest() throws IOException {
        var bs = BScript.fromSource(
                """
                   def test(int i):
                   \tloop:
                   \t\ti=i+1
                   \t\tif(i>10):
                   \t\t\tbreak
                   """);
        bs.compile();
        System.out.println(bs.tokenStream);
    }
}
