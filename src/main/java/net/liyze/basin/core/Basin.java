package net.liyze.basin.core;

import net.liyze.basin.interfaces.BasinBoot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static net.liyze.basin.core.Main.BootClasses;

@SuppressWarnings({"SameReturnValue"})
public final class Basin {
    @SuppressWarnings("SpellCheckingInspection")
    public static String basin = String.format(
            """
                    \r
                    BBBBBBBBBBBBBBBBB                                         iiii
                    B::::::::::::::::B                                       i::::i
                    B::::::BBBBBB:::::B                                       iiii
                    BB:::B      B:::::B
                    B::::B       B:::::B   aaaaaaaaaaaaa        ssssssssss   iiiiiii   nnnn  nnnnnnnn
                    B::::B      B:::::B   a::::::::::::a     ss::::::::::s   i:::::i  n:::nn::::::::nn
                    B::::BBBBBB:::::B     aaaaaaaaa:::::a  ss:::::::::::::s   i::::i  n::::::::::::::nn
                    B::::::::::::BB                a::::a  s::::::ssss:::::s  i::::i  nn::::::::::::::n
                    B::::BBBBBB:::::B       aaaaaaa:::::a   s:::::s   ssssss  i::::i   n:::::nnnn:::::n
                    B::::B      B:::::B    aa::::::::::::a     s::::::s       i::::i   n::::n    n::::n
                    B::::B       B:::::B  a::::aaaa::::::a        s::::::s    i::::i   n::::n    n::::n
                    B::::B      B:::::B  a::::a    a:::::a  ssssss   s:::::s  i::::i   n::::n    n::::n
                    BB:::::BBBBBB::::::B a::::a    a:::::a s:::::ssss::::::s i::::::i  n::::n    n::::n
                    B:::::::::::::::::B a:::::aaaa::::::a  s::::::::::::::s  i::::::i  n::::n    n::::n
                    B::::::::::::::::B   a::::::::::aa:::a  s:::::::::::ss   i::::::i  n::::n    n::::n
                    BBBBBBBBBBBBBBBBB     aaaaaaaaaa  aaaa   sssssssssss     iiiiiiii  nnnnnn    nnnnnn
                    :: Basin :: (%s)
                    """, getVersion());

    @Contract(pure = true)
    public static @NotNull String getVersion() {
        return "0.1";
    }

    public static int getVersionNum() {
        return 1;
    }

    /**
     * Stop basin after all task finished.
     */
    public static void shutdown() {
        Main.LOGGER.info("Stopping");
        BootClasses.forEach((i) -> {
            try {
                ((BasinBoot) i.getDeclaredConstructor().newInstance()).beforeStop();
            } catch (Exception ignored) {
            }
        });
        Main.taskPool.shutdown();
        Main.servicePool.shutdownNow();
        System.exit(0);
    }
}
