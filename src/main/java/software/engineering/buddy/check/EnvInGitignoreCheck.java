package software.engineering.buddy.check;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class EnvInGitignoreCheck implements AutoCheck {
  @Override
  public boolean run(Path root) {
    try {
      Path gitignore = root.resolve(".gitignore");
      if (!Files.exists(gitignore)) return false;
      List<String> lines = Files.readAllLines(gitignore);
      // Check if any line contains .env
      return lines.stream().anyMatch(line -> line.trim().matches("^\\.env.*|^env.*|\\.env"));
    } catch (Exception e) {
      return false;
    }
  }
}
