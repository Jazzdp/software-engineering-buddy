package software.engineering.buddy.check;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CiWorkflowCheck implements FixableCheck {
    @Override
    public boolean run(Path root) {
        return Files.isDirectory(root.resolve(".github/workflows"));
    }

    @Override
    public void fix(Path root) throws IOException {
        Path workflowDir = root.resolve(".github/workflows");
        Files.createDirectories(workflowDir);
        
        String yaml = """
            name: Build and Test
            on: [push, pull_request]
            jobs:
              build:
                runs-on: ubuntu-latest
                steps:
                - uses: actions/checkout@v4
                - name: Set up JDK 21
                  uses: actions/setup-java@v4
                  with:
                    java-version: '21'
                    distribution: 'temurin'
                - name: Setup Gradle
                  uses: gradle/actions/setup-gradle@v3
                - name: Build with Gradle
                  run: ./gradlew build
            """;
        
        Files.writeString(workflowDir.resolve("build.yml"), yaml);
    }
}