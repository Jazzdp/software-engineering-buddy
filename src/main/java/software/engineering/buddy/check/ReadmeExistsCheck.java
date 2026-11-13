package software.engineering.buddy.check;

import java.nio.file.Files;
import java.nio.file.Path;

public class ReadmeExistsCheck implements AutoCheck {
  @Override
  public boolean run(Path root) {
    return Files.exists(root.resolve("README.md"));
  }
}
