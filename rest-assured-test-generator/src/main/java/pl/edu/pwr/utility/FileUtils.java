package pl.edu.pwr.utility;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileUtils {
    public static CharStream readInputFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!path.isAbsolute()) {
                path = path.toAbsolutePath().normalize();
            }

            if (!Files.exists(path)) {
                System.err.println("File not found: " + path);
                return null;
            }

            return CharStreams.fromPath(path);
        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }
    }

    public static void saveToFile(String pathString, String content) {
        try {
            Path path = Paths.get(pathString);

            if (!path.isAbsolute()) {
                path = path.toAbsolutePath().normalize();
            }

            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("File written successfully to: " + path);
        } catch (Exception e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
