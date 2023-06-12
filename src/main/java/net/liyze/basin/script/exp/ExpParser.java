package net.liyze.basin.script.exp;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("unused")
public class ExpParser {
    public String name;
    protected final Queue<Code> queue = new ConcurrentLinkedQueue<>();
    protected static final Map<String, Keywords> keywords;

    static {
        Map<String, Keywords> rt = new HashMap<>();
        Arrays.stream(Keywords.values()).forEach(key -> rt.put(key.name(), key));
        keywords = Collections.unmodifiableMap(rt);
    }

    public ExpParser() {
    }

    public boolean serialize() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("data"+File.separator+"out"+File.separator+this.name+".bsc"));
            outputStream.writeObject(queue);
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<String> getLines(Reader r) throws IOException {
        BufferedReader reader = new BufferedReader(r);
        List<String> lines = reader.lines().toList();
        reader.close();
        return lines;
    }

    public void parseFromLines(List<String> lines) {

    }
}
