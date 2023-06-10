package net.liyze.basin.script.preparser;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.context.annotation.WithoutInstance;
import net.liyze.basin.script.Parser;
import net.liyze.basin.script.PreParser;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@Component
@WithoutInstance
public class Add extends PreParser {
    public Add(Parser parser) {
        super(parser);
    }

    @Override
    public @NotNull String getRegex() {
        return ".+\\+.+";
    }

    @Override
    public String apply(String s) {
        String[] x = StringUtils.split(s, '+');
        String y;
        try {
            y = String.valueOf(Double.parseDouble(x[0]) + Double.parseDouble(x[0]));
        } catch (Exception e) {
            y = x[0] + x[1];
        }
        return y;
    }
}
