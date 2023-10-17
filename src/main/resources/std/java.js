var exp = {}
exp.File = Java.type("java.io.File");
exp.Thread = Java.type("java.lang.Thread");
exp.Basin = Java.type("net.liyze.basin.BasinFramework").INSTANCE
exp.FileInputStream = Java.type("java.io.FileInputStream")
exp.FileOutputStream = Java.type("java.io.FileOutputStream")
exp.JString = Java.type("java.lang.String")
this.export("java", exp)