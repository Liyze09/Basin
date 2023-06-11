package net.liyze.basin.script.preparser;

import net.liyze.basin.context.annotation.Component;
import net.liyze.basin.context.annotation.WithoutInstance;
import net.liyze.basin.script.AbstractPreParser;
import net.liyze.basin.script.Parser;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@Component
@WithoutInstance
public class Less extends AbstractPreParser {

    public Less(Parser parser) {
        super(parser);
    }

    @Override
    public @NotNull String getRegex() {
        return ".+<.+";
    }

    @Override
    public String apply(String s) {
        String[] min = StringUtils.split(s,"<");
        return String.valueOf((Double.parseDouble(min[0])<Double.parseDouble(min[1])));
    }
}
