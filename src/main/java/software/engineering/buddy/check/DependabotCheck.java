package software.engineering.buddy.check;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DependabotCheck implements FixableCheck {
    @Override
    public boolean run(Path root) {
        return Files.exists(root.resolve(".github/dependabot.yml"));
    }

    @Override
    public void fix(Path root) throws IOException {
        Path githubDir = root.resolve(".github");
        Files.createDirectories(githubDir);
        
        String yaml = """
            version: 2
            updates:
              - package-ecosystem: "gradle"
                directory: "/"
                schedule:
                  interval: "weekly"
            """;
        
        Files.writeString(githubDir.resolve("dependabot.yml"), yaml);
    }
}