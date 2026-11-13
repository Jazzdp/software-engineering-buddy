package software.engineering.buddy.check;

import java.nio.file.Files;
import java.nio.file.Path;

public class LicenseExistsCheck implements AutoCheck {
  @Override
  public boolean run(Path root) {
    return Files.exists(root.resolve("LICENSE")) || Files.exists(root.resolve("LICENSE.md"));
  }
}
