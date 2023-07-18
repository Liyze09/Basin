package bscript;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BScriptJarBuilder {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void toJar(@NotNull Map<String, byte[]> classes, String fileName) {
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file))) {
            StringBuilder builder = new StringBuilder();
            classes.forEach((name, bytes) -> {
                try {
                    zip.putNextEntry(new ZipEntry(name.replace('.', '/') + ".class"));
                    zip.write(bytes);
                    zip.closeEntry();
                    builder.append(name).append("\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            zip.putNextEntry(new ZipEntry("beans"));
            zip.write(builder.toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("bscript/BScriptRuntime.class".replace('/', File.separatorChar)));
            zip.write(Files.readAllBytes(Path.of(Objects.requireNonNull(BScriptJarBuilder.class.getResource("/bscript/BScriptRuntime.class")).getPath())));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("bscript/BScriptExecutableFile.class".replace('/', File.separatorChar)));
            zip.write(Files.readAllBytes(Path.of(Objects.requireNonNull(BScriptJarBuilder.class.getResource("/bscript/BScriptExecutableFile.class")).getPath())));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("bscript/exception/LoadFailedException.class".replace('/', File.separatorChar)));
            zip.write(Files.readAllBytes(Path.of(Objects.requireNonNull(BScriptJarBuilder.class.getResource("/bscript/exception/LoadFailedException.class")).getPath())));
            zip.closeEntry();

            putClass(zip, "bscript.exception.BScriptException");
            putClass(zip, "bscript.exception.BroadcastException");
            putClass(zip, "bscript.BScriptRuntime$MultiMap");
            putClass(zip, "bscript.BScriptClassLoader");
            putClass(zip, "bscript.BScriptEvent");

            zip.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            zip.write("Manifest-Version: 1.0\nMain-Class: bscript.BScriptExecutableFile\n\n".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void putClass(ZipOutputStream zip, String clazz) throws IOException {
        String file = clazz.replace('.', File.separatorChar) + ".class";
        zip.putNextEntry(new ZipEntry(file));
        try (InputStream input = BScriptJarBuilder.class.getResourceAsStream("/" + file)) {
            if (input != null) {
                zip.write(input.readAllBytes());
            }
        }
        zip.closeEntry();
    }
}
