package software.engineering.buddy.check;

import java.io.IOException;
import java.nio.file.Path;

public interface FixableCheck extends AutoCheck {
    void fix(Path root) throws IOException;
}