package bscript;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class BScriptJarBuilderTest {
    @Disabled
    @Test
    void toJar() {
        new BScriptJarBuilder().toJar(BScriptHelper.getInstance().compile("t.Test", """
                handle main {
                 print("Hello, World!")
                }
                """), "data/output/T.jar");
    }
}