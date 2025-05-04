import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class ProjectTest extends StageTest<Void> {
    /**
     * Checks files: project/Main.class, MANIFEST.MF, app.jar
     */
    @DynamicTest
    CheckResult test1() {
        File dockerfile = new File("Dockerfile");
        if (!dockerfile.exists()) {
            return CheckResult.wrong("The 'Dockerfile' was not found in the project directory.");
        }

        String content;
        try {
            content = Files.readString(dockerfile.toPath());
        } catch (IOException e) {
            return CheckResult.wrong("Error reading 'Dockerfile': " + e.getMessage());
        }

        if (!content.contains("FROM") || !content.contains("COPY") || !content.contains("ENTRYPOINT")) {
            return CheckResult.wrong("The 'Dockerfile' does not contain the necessary instructions: FROM, COPY, and ENTRYPOINT.");
        }

        return CheckResult.correct();
    }

    /**
     * Checks if the Docker is running
     */
    @DynamicTest
    CheckResult test2() {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "-v");
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            BufferedReader out = new BufferedReader(
                    new InputStreamReader(proc.getInputStream())
            );

            String line = out.readLine();
            if (line == null || !line.contains("version")) {
                return CheckResult.wrong(
                        "Docker is not installed or not running. " +
                                "Please install Docker and make sure it is running."
                );
            }
        } catch (Exception e) {
            return CheckResult.wrong(
                    "Docker is not running. Please start Docker and try again."
            );
        }

        return CheckResult.correct();
    }

        /**
         * Checks if the Docker image named 'java-in-docker' exists.
         */
    @DynamicTest
    CheckResult test3() {

        try {
            // List images filtered by our repo name
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "images",
                    "--filter", "reference=java-in-docker",
                    "--format", "{{.Repository}}"
            );
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            BufferedReader out = new BufferedReader(
                    new InputStreamReader(proc.getInputStream())
            );
            String line = out.readLine();
            int exit = proc.waitFor();

            if (exit != 0) {
                return CheckResult.wrong(
                        "Could not list Docker images. Make sure Docker is running."
                );
            }

            if (line == null || !line.equals("java-in-docker")) {
                return CheckResult.wrong(
                        "Docker image named 'java-in-docker' not found. " +
                                "Did you build your image with `-t java-in-docker`?"
                );
            }

        } catch (Exception e) {
            return CheckResult.wrong(
                    "Error while checking Docker images: " + e.getMessage()
            );
        }

        return CheckResult.correct();
    }
}