package bscript;

public class BScriptClassLoader extends ClassLoader {
    private final byte[] clazz;

    public BScriptClassLoader(byte[] clazz) {
        this.clazz = clazz;
    }

    @Override
    protected Class<?> findClass(String name) {
        return defineClass(name, clazz, 0, clazz.length);
    }
}
