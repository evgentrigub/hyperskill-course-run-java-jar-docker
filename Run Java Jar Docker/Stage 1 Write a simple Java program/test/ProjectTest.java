import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import java.io.File;
import org.hyperskill.hstest.testing.TestedProgram;

public class ProjectTest extends StageTest<Void> {

    /**
     * Checks if the program output works correctly.
     */
    @DynamicTest
    CheckResult test1() {
        TestedProgram program = new TestedProgram();
        String output = program.start();
        
        if (!output.toLowerCase().contains("enter your name")) {
            return CheckResult.wrong("Your program should ask for the user's name with the message 'Enter your name:'");
        }

        String testName = "John";
        output = program.execute(testName);
        
        if (!output.contains("Hello, " + testName + "!")) {
            return CheckResult.wrong("Your program should output 'Hello' and the user's name!");
        }
        
        if (!output.contains("Greetings from Docker")) {
            return CheckResult.wrong("Your program should output 'Greetings from Docker'!");
        }

        return CheckResult.correct();
    }

    /**
     * Checks if the build directory exists.
     */
    @DynamicTest
    CheckResult test2() {
        var currentDir = System.getProperty("user.dir");
        File buildDir = new File("build_dir");
        if (!buildDir.exists() || !buildDir.isDirectory()) {
            return CheckResult.wrong(
                    "The 'build_dir' directory was not found in 'stage1' directory. " +
                    "Check you current path with 'pwd' command and make sure you are in the correct directory. " +
                    "The build directory should be in this specific directory " + currentDir);
        }

        return CheckResult.correct();
    }

    /**
     * Checks if the Main.class file exists in the build directory.
     */
    @DynamicTest
    CheckResult test3() {
        File buildDir = new File("build_dir");
        File mainClassFile = new File(buildDir, "project/Main.class");
        if (!mainClassFile.exists()) {
            return CheckResult.wrong("The 'Main.class' file was not found in the 'build' directory. Ensure you compiled your Java file correctly.");
        }

        return CheckResult.correct();
    }
}