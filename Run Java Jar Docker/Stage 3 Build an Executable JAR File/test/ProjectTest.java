import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ProjectTest extends StageTest<Void> {
    /**
     * Checks files: project/Main.class, MANIFEST.MF, app.jar
     */
    @DynamicTest
    CheckResult test1() {
        File mainClassFile = new File("build_dir", "project/Main.class");
        if (!mainClassFile.exists()) {
            return CheckResult.wrong("The 'Main.class' file was not found in the 'build_dir' directory. Ensure you compiled your Java file correctly.");
        }

        File manifestFileInBuild = new File("build_dir/MANIFEST.MF");
        if (!manifestFileInBuild.exists()) {
            return CheckResult.wrong("The 'MANIFEST.MF' file was not found in the 'build' directory.");
        }

        File jarFile = new File("build_dir/app.jar");
        if (!jarFile.exists()) {
            return CheckResult.wrong("The 'app.jar' file was not found in the 'build' directory.");
        }

        return CheckResult.correct();
    }

    /**
     * Checks if the 'app.jar' file exists and contains the required entries: project/Main.class, META-INF/, META-INF/MANIFEST.MF
     */
    @DynamicTest
    CheckResult test2() {
        File jarFile = new File("build_dir/app.jar");
        if (!jarFile.exists()) {
            return CheckResult.wrong("The 'app.jar' file was not found in the 'build_dir' directory.");
        }

        Set<String> expectedEntries = Set.of("project/Main.class", "META-INF/", "META-INF/MANIFEST.MF");

        Set<String> actualEntries = new HashSet<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                actualEntries.add(entries.nextElement().getName());
            }
        } catch (IOException e) {
            return CheckResult.wrong("An error occurred while reading 'app.jar': " + e.getMessage());
        }

        final String HINT = "Your build_dir should contain only project/Main.class and MANIFEST.MF before you create jar file.";
        if (!actualEntries.containsAll(expectedEntries)) {
            return CheckResult.wrong(
                    "The 'app.jar' does not contain required entries. Expected entries: " + expectedEntries +
                            ", but found: " + actualEntries + ". " + HINT

            );
        }

        if (actualEntries.size() != expectedEntries.size()) {
            return CheckResult.wrong(
                    "The 'app.jar' contains unexpected additional entries. Expected exactly: " +
                            expectedEntries + ", but found: " + actualEntries + ". " + HINT
            );
        }

        return CheckResult.correct();
    }

    /**
     * Checks if the 'app.jar' file can be executed and produces the expected output.
     */
    @DynamicTest
    CheckResult test3() {
        File jarFile = new File("build_dir/app.jar");
        if (!jarFile.exists()) {
            return CheckResult.wrong("The 'app.jar' file was not found in the 'build_dir' directory.");
        }
        try {
            Process process = new ProcessBuilder("java", "-jar", jarFile.getAbsolutePath())
                    .redirectErrorStream(true)
                    .start();

            // Add input to the process
            process.getOutputStream().write("John\n".getBytes());
            process.getOutputStream().flush();
            process.getOutputStream().close();
            // Read the output
            StringBuilder output = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Check the output
            String outputString = output.toString();
            if (!outputString.contains("Hello, John!") || !outputString.contains("Greetings from Docker")) {
                return CheckResult.wrong("The 'app.jar' file did not produce the expected output. " +
                        "Output: " + outputString + ". Expected: 'Hello, John!' and 'Greetings from Docker' if the input was 'John'.");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return CheckResult.wrong("The 'app.jar' file could not be executed. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            return CheckResult.wrong("An error occurred while executing 'app.jar': " + e.getMessage());
        }

        return CheckResult.correct();
    }
}