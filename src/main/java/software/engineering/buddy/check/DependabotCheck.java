package software.engineering.buddy.check;

import java.nio.file.Files;
import java.nio.file.Path;

public class DependabotCheck implements AutoCheck {
  @Override
  public boolean run(Path root) {
    return Files.exists(root.resolve(".github/dependabot.yml"))
        || Files.exists(root.resolve(".github/dependabot.yaml"));
  }
}
