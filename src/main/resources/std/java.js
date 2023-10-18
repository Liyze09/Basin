var exp = {}
exp.File = Java.type("java.io.File");
exp.Thread = Java.type("java.lang.Thread");
exp.Basin = Java.type("net.liyze.basin.BasinFramework")
exp.FileInputStream = Java.type("java.io.FileInputStream")
exp.FileOutputStream = Java.type("java.io.FileOutputStream")
exp.JString = Java.type("java.lang.String")
exp.MessageDigest = Java.type("java.security.MessageDigest")
this.export("java", exp)