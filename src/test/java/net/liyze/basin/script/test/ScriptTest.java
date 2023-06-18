package net.liyze.basin.script.test;

import bscript.DefaultBScriptHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BScript Test 1")
public final class ScriptTest{
    @Test
    public void scriptTest() {
        var bs = DefaultBScriptHandler.fromSource(
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
