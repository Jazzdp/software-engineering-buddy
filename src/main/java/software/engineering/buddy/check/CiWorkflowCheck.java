package software.engineering.buddy.check;

import java.nio.file.Files;
import java.nio.file.Path;

public class CiWorkflowCheck implements AutoCheck {
  @Override
  public boolean run(Path root) {
    return Files.isDirectory(root.resolve(".github/workflows"));
  }
}
