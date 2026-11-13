package software.engineering.buddy.check;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestsFolderCheck implements AutoCheck {
  @Override
  public boolean run(Path root) {
    return Files.isDirectory(root.resolve("src/test"))
        || Files.isDirectory(root.resolve("test"))
        || Files.isDirectory(root.resolve("tests"));
  }
}
