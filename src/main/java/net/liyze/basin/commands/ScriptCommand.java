package net.liyze.basin.commands;

import net.liyze.basin.Command;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static net.liyze.basin.RunCommands.runCommand;

public class ScriptCommand implements Command {
    @Override
    public void run(ArrayList<String> args) {
        try (
                BufferedReader script = new BufferedReader(new FileReader("data"+File.separator+"script"+File.separator+args.get(0), StandardCharsets.UTF_8))
        ){
            Object[] lines = script.lines().toArray();
            for (Object line : lines) {
                if (!((String) line).isEmpty()) runCommand((String) line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String Name() {
        return "script";
    }
}
