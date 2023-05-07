package net.liyze.liyzetools;

import java.util.ArrayList;

import static net.liyze.liyzetools.Main.commands;
import static net.liyze.liyzetools.util.Out.error;

public abstract class runCommands {
    public static void runCommand(String cmd) {
        cmd = cmd + " ";
        var sb = new StringBuilder(128);
        char[] rt1 = cmd.toCharArray();
        ArrayList<String> args = new ArrayList<>();
        ArrayList<Character> nameList = new ArrayList<>();
        ArrayList<Character> rt2 = new ArrayList<>();
        String cmdName = "";
        int arg = 0;
        for (char a : rt1) {
            if (!(a == ' ')) {
                if (arg == 0) {
                    nameList.add(a);
                } else {
                    rt2.add(a);
                }
            } else {
                if (!(arg == 0)) {
                    for (char b : rt2) {
                        sb.append(b);
                    }
                    args.add(sb.toString());
                    rt2 = new ArrayList<>();
                } else {
                    for (char b : nameList) {
                        sb.append(b);
                    }
                    cmdName = sb.toString();
                }
                ++arg;
                sb = new StringBuilder();
            }
        }
        Command run = commands.get(cmdName);
        if (!(run == null)) {
            try {
                run.run(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else error("Unknown command: " + cmdName);
    }
}
