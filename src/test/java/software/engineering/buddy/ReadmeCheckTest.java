package software.engineering.buddy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.engineering.buddy.check.ReadmeExistsCheck;

class ReadmeCheckTest {

  @TempDir Path tempDir;

  @Test
  void failsWhenReadmeIsMissing() {
    ReadmeExistsCheck check = new ReadmeExistsCheck();
    assertFalse(check.run(tempDir));
  }

  @Test
  void passesWhenReadmeExists() throws IOException {
    Files.createFile(tempDir.resolve("README.md"));
    ReadmeExistsCheck check = new ReadmeExistsCheck();
    assertTrue(check.run(tempDir));
  }
}
