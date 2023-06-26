package net.liyze.basin.core.commands;

import com.itranswarp.summer.context.annotation.Component;
import net.liyze.basin.core.Command;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EquationCommand implements Command {
    static HashMap<String, Double> leftUnknowns = new HashMap<>();
    static HashMap<String, Double> rightUnknowns = new HashMap<>();

    private static void addUnknown(String name, double coefficient, boolean isLeft) {
        double g;
        if (isLeft) {
            if (leftUnknowns.containsKey(name)) {
                g = leftUnknowns.get(name);
                g += coefficient;
                leftUnknowns.put(name, g);
            } else {
                leftUnknowns.put(name, coefficient);
            }
        } else {
            if (rightUnknowns.containsKey(name)) {
                g = rightUnknowns.get(name);
                g += coefficient;
                rightUnknowns.put(name, g);
            } else {
                rightUnknowns.put(name, coefficient);
            }
        }
    }

    @Override
    public void run(@NotNull List<String> args) throws RuntimeException {
        String es = args.get(0).toLowerCase().strip().replaceAll("[*]", "");
        char[] e = es.toCharArray();
        char[] x = es.replaceAll("[^a-z]", "").toCharArray();
        String h = "h";
        ArrayList<String> left = new ArrayList<>();
        ArrayList<String> right = new ArrayList<>();
        double leftNum = 0;
        double rightNum = 0;
        double allNum;
        boolean or = true;
        String ie;
        //Check
        for (char c : e) {
            ie = String.valueOf(c);
            if (ie.equals("/")) {
                throw new RuntimeException("No \"/\"");
            }
            if (ie.equals("=") || ie.equals(">") || ie.equals("<")) {
                h = ie;
                or = false;
            } else {
                if (or) left.add(ie);
                else right.add(ie);
            }
        }
        int bf;
        double ci;
        var sb = new StringBuilder();
        //Left
        for (int i = 0; i < left.size(); ++i) {
            ie = left.get(i);
            bf = i;
            if (ie.matches("[+-]")) {
                ++i;
                ie = left.get(i);
                while (ie.matches("[0-9]") && i < left.size()) {
                    ie = left.get(i);
                    if (ie.matches("[0-9]")) sb.append(ie);
                    ++i;
                }
                String s = sb.toString();
                ci = Double.parseDouble(s);
                if (left.get(bf).equals("-")) {
                    ci = -ci;
                }
                if (ie.matches("[a-z]")) {
                    addUnknown(ie, ci, true);
                } else if (ie.matches("[+-]")) {
                    leftNum += ci;
                } else if (ie.matches("[0-9]")) {
                    leftNum += ci;
                } else {
                    throw new RuntimeException("Unknown: " + ie);
                }
                sb = new StringBuilder();
            } else if (ie.matches("[0-9]")) {
                while (ie.matches("[0-9]") && i < left.size()) {
                    ie = left.get(i);
                    if (ie.matches("[0-9]")) sb.append(ie);
                    ++i;
                }
                String s = sb.toString();
                ci = Double.parseDouble(s);
                if (ie.matches("[a-z]")) {
                    addUnknown(ie, ci, true);
                } else if (ie.matches("[+-]")) {
                    leftNum += ci;
                } else if (ie.matches("[0-9]")) {
                    leftNum += ci;
                } else {
                    throw new RuntimeException("Unknown: " + ie);
                }
                sb = new StringBuilder();
            } else if (ie.matches("[a-z]")) {
                addUnknown(ie, 1, true);
            } else {
                throw new RuntimeException("Unknown: " + ie);
            }
        }
        sb = new StringBuilder();
        //Right
        for (int i = 0; i < right.size(); ++i) {
            ie = right.get(i);
            bf = i;
            if (ie.matches("[+-]")) {
                ++i;
                ie = right.get(i);
                while (ie.matches("[0-9]") && i < right.size()) {
                    ie = right.get(i);
                    if (ie.matches("[0-9]")) sb.append(ie);
                    ++i;
                }
                String s = sb.toString();
                ci = Double.parseDouble(s);
                if (right.get(bf).equals("-")) {
                    ci = -ci;
                }
                if (ie.matches("[a-z]")) {
                    addUnknown(ie, ci, false);
                } else if (ie.matches("[+-]")) {
                    rightNum += ci;
                } else if (ie.matches("[0-9]")) {
                    rightNum += ci;
                } else {
                    throw new RuntimeException("Unknown: " + ie);
                }
                sb = new StringBuilder();
            } else if (ie.matches("[0-9]")) {
                while (ie.matches("[0-9]") && i < right.size()) {
                    ie = right.get(i);
                    if (ie.matches("[0-9]")) sb.append(ie);
                    ++i;
                }
                String s = sb.toString();
                ci = Double.parseDouble(s);
                if (ie.matches("[a-z]")) {
                    addUnknown(ie, ci, false);
                } else if (ie.matches("[+-]")) {
                    rightNum += ci;
                } else if (ie.matches("[0-9]")) {
                    rightNum += ci;
                } else {
                    throw new RuntimeException("Unknown: " + ie);
                }
                sb = new StringBuilder();
            } else if (ie.matches("[a-z]")) {
                addUnknown(ie, 1, false);
            } else {
                throw new RuntimeException("Unknown: " + ie);
            }
        }
        allNum = rightNum - leftNum;
        for (Map.Entry<String, Double> entry : rightUnknowns.entrySet()) {
            addUnknown(entry.getKey(), -(entry.getValue()), true);
        }
        ArrayList<Double> solution = new ArrayList<>();
        if (leftUnknowns.size() == 1) {
            double v = leftUnknowns.get(String.valueOf(x[0]));
            if (v < 0) {
                if (h.equals(">")) h = "<";
                if (h.equals("<")) h = ">";
            } else if (v == 0) h = "=";
            solution.add(allNum / v);
        }
        double s;
        char xx;
        try {
            for (int i = 0; i < x.length; ++i) {
                xx = x[i];
                s = solution.get(i);
                System.out.println(xx + h + s);
            }
        } catch (IndexOutOfBoundsException ignored) {
        }
        leftUnknowns.clear();
        rightUnknowns.clear();
    }

    @Override
    public @NotNull String Name() {
        return "equation";
    }
}