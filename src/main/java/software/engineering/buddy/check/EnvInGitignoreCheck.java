package software.engineering.buddy.check;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class EnvInGitignoreCheck implements FixableCheck {
    @Override
    public boolean run(Path root) {
        try {
            Path gitignore = root.resolve(".gitignore");
            if (!Files.exists(gitignore)) return false;
            List<String> lines = Files.readAllLines(gitignore);
            return lines.stream().anyMatch(line -> line.trim().matches("^\\.env.*|^env.*|\\.env"));
        } catch (Exception e) {
            return false;
        }
    }
        @Override
    public void fix(Path root) throws IOException {
        Path gitignore = root.resolve(".gitignore");
        String content = Files.exists(gitignore) ? Files.readString(gitignore) : "";
        
        if (!content.contains(".env")) {
            if (!content.endsWith("\n")) content += "\n";
            content += "\n# Added by sebuddy for security\n.env\n.env.local\n.env.*\n";
            Files.writeString(gitignore, content);
        }
    }
}