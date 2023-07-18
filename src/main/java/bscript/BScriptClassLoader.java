package bscript;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * A ClassLoader that load a ByteArray.
 */
public class BScriptClassLoader extends URLClassLoader {
    private final Map<String, byte[]> classBytes = new HashMap<>();

    public BScriptClassLoader(Map<String, byte[]> classBytes) {
        super(new URL[0], BScriptClassLoader.class.getClassLoader());
        this.classBytes.putAll(classBytes);
    }

    public BScriptClassLoader() {
        super(new URL[0], BScriptClassLoader.class.getClassLoader());
    }

    public void put(String name, byte[] bytes) {
        classBytes.put(name, bytes);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] buf = classBytes.get(name);
        if (buf == null) {
            return super.findClass(name);
        }
        classBytes.remove(name);
        return defineClass(name, buf, 0, buf.length);
    }

}
