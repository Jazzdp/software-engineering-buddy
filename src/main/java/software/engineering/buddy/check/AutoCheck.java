package software.engineering.buddy.check;

import java.nio.file.Path;

public interface AutoCheck {
  boolean run(Path root);
}
