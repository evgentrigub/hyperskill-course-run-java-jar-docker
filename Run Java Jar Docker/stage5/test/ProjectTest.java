import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProjectTest extends StageTest<Void> {
    /**
     * Checks if Docker has a running container with the name "java-in-docker".
     */
    @DynamicTest
    CheckResult test1() {
        // Check if Docker is installed and running
        try {
            ProcessBuilder builder = new ProcessBuilder("docker", "-v");
            builder.redirectErrorStream(true);
            Process process = builder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return CheckResult.wrong("Docker is not installed or not running. Please install Docker and start the Docker daemon.");
            }
        } catch (IOException | InterruptedException e) {
            return CheckResult.wrong("An error occurred while checking Docker: " + e.getMessage());
        }

        // Check if the container is running
        try {
            ProcessBuilder builder = new ProcessBuilder("docker", "ps", "-a");
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean containerFound = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("java-in-docker-container")) {
                    containerFound = true;
                    break;
                }
            }
            if (!containerFound) {
                return CheckResult.wrong("Docker container with the name 'java-in-docker-container' from image is not running. Please run the Docker container.");
            }
        } catch (IOException e) {
            return CheckResult.wrong("An error occurred while checking Docker container: " + e.getMessage());
        }

        return CheckResult.correct();
    }

    /**
     * Check if the container is run correctly from the image 'java-in-docker'.
     */
    @DynamicTest
    CheckResult test2() {
        try {
            ProcessBuilder builder = new ProcessBuilder("docker", "run", "--rm", "-i", "java-in-docker");
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // Write input in a separate thread to avoid potential deadlocks
            Thread inputThread = new Thread(() -> {
                try {
                    process.getOutputStream().write("John\n".getBytes());
                    process.getOutputStream().flush();
                    process.getOutputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            inputThread.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return CheckResult.wrong("Docker wasn't complete properly. Docker process exited with code " + exitCode);
            }

            String outputStr = output.toString();
            if (!outputStr.contains("Hello, John!") || !outputStr.contains("Greetings from Docker!")) {
                return CheckResult.wrong("The application did not output the expected message: 'Hello, John! Greeting from Docker!' if your input was 'John'. " +
                                "The output was:\n" + outputStr);
            }

        } catch (Exception e) {
            return CheckResult.wrong("An error occurred while running the Docker container: " + e.getMessage());
        }

        return CheckResult.correct();
    }

}