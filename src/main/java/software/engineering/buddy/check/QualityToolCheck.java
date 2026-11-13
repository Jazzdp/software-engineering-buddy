package software.engineering.buddy.check;

import java.nio.file.Files;
import java.nio.file.Path;

public class QualityToolCheck implements AutoCheck {
  @Override
  public boolean run(Path root) {
    try {
      Path gradleFile = root.resolve("build.gradle");
      if (!Files.exists(gradleFile)) gradleFile = root.resolve("build.gradle.kts");
      if (Files.exists(gradleFile)) {
        String content = Files.readString(gradleFile);
        // Look for JaCoCo (code coverage) or Spotless/Checkstyle (formatting)
        return content.contains("jacoco")
            || content.contains("spotless")
            || content.contains("checkstyle");
      }

      Path npmFile = root.resolve("package.json");
      if (Files.exists(npmFile)) {
        String content = Files.readString(npmFile);
        // Look for ESLint or Prettier
        return content.contains("eslint") || content.contains("prettier");
      }
      return false;
    } catch (Exception e) {
      return false;
    }
  }
}
