import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ProjectTest extends StageTest<Void> {


    /**
     * Checks if the 'MANIFEST.MF' file is present in the 'resources' and 'build_dir' directories
     */
    @DynamicTest
    CheckResult test1() {
        File manifestFile = new File("src/main/resources/META-INF/MANIFEST.MF");
        if (!manifestFile.exists()) {
            return CheckResult.wrong("The 'MANIFEST.MF' file was not found in the 'resources' directory.");
        }

        File manifestFileInBuild = new File("build_dir/MANIFEST.MF");
        if (!manifestFileInBuild.exists()) {
            return CheckResult.wrong("The 'MANIFEST.MF' file was not found in the 'build' directory.");
        }

        return CheckResult.correct();
    }

    /**
     * Checks if the 'MANIFEST.MF' file contains the correct 'Manifest-Version' and 'Main-Class' attributes
     */
    @DynamicTest
    CheckResult test2() {
        File manifestFile = new File("build_dir/MANIFEST.MF");

        String content;
        try {
            content = Files.readString(manifestFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return CheckResult.wrong("Error reading 'MANIFEST.MF': " + e.getMessage());
        }

        if (!content.contains("Manifest-Version: 1.0")) {
            return CheckResult.wrong("The 'MANIFEST.MF' file does not specify 'Manifest-Version: 1.0'.");
        }

        if (!content.contains("Main-Class: project.Main")) {
            return CheckResult.wrong("The 'MANIFEST.MF' file does not specify 'Main-Class: project.Main'.");
        }

        return CheckResult.correct();
    }
}