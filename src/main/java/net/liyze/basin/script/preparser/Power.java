package net.liyze.basin.script.preparser;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.context.annotation.WithoutInstance;
import net.liyze.basin.script.AbstractPreParser;
import net.liyze.basin.script.Parser;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
@Component
@WithoutInstance
public class Power extends AbstractPreParser {
    public Power(Parser parser) {
        super(parser);
    }

    @Override
    public @NotNull String getRegex() {
        return ".+\\^.+";
    }

    @Override
    public String apply(String s) {
        String[] min = StringUtils.split(s, "^");
        return String.valueOf(Integer.parseInt(min[0])^Integer.parseInt(min[1]));
    }
}
