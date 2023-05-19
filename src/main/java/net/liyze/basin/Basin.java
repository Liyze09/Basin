package net.liyze.basin;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static net.liyze.basin.Main.*;
import static net.liyze.basin.util.Out.info;

@SuppressWarnings({"SameReturnValue", "unused"})
public final class Basin {
    @Contract(pure = true)
    public static @NotNull String getVersion() {
        return "0.1";
    }

    public static int getVersionNum() {
        return 1;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static String basin =
            """
                                    BBBBBBBBBBBBBBBBB                                        iiii
                                    B::::::::::::::::B                                      i::::i
                                    B::::::BBBBBB:::::B                                      iiii
                                    BB:::::B     B:::::B
                                      B::::B     B:::::B  aaaaaaaaaaaaa       ssssssssss   iiiiiii nnnn  nnnnnnnn
                                      B::::B     B:::::B  a::::::::::::a    ss::::::::::s  i:::::i n:::nn::::::::nn
                                      B::::BBBBBB:::::B   aaaaaaaaa:::::a ss:::::::::::::s  i::::i n::::::::::::::nn
                                      B:::::::::::::BB             a::::a s::::::ssss:::::s i::::i nn:::::::::::::::n
                                      B::::BBBBBB:::::B     aaaaaaa:::::a  s:::::s  ssssss  i::::i   n:::::nnnn:::::n
                                      B::::B     B:::::B  aa::::::::::::a    s::::::s       i::::i   n::::n    n::::n
                                      B::::B     B:::::B a::::aaaa::::::a       s::::::s    i::::i   n::::n    n::::n
                                      B::::B     B:::::Ba::::a    a:::::a ssssss   s:::::s  i::::i   n::::n    n::::n
                                    BB:::::BBBBBB::::::Ba::::a    a:::::a s:::::ssss::::::si::::::i  n::::n    n::::n
                                    B:::::::::::::::::B a:::::aaaa::::::a s::::::::::::::s i::::::i  n::::n    n::::n
                                    B::::::::::::::::B   a::::::::::aa:::a s:::::::::::ss  i::::::i  n::::n    n::::n
                                    BBBBBBBBBBBBBBBBB     aaaaaaaaaa  aaaa  sssssssssss    iiiiiiii  nnnnnn    nnnnnn
                                       
                    """;

    public static void shutdown() {
        info("Stopping!");
        scanCmd.interrupt();
        taskPool.shutdown();
        servicePool.shutdownNow();
        System.exit(0);
    }
}
