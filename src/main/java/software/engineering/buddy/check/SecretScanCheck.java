package software.engineering.buddy.check;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SecretScanCheck implements AutoCheck {
    
    private static final Pattern[] SECRET_PATTERNS = {
        Pattern.compile("(?i)(password|passwd|pwd)\\s*=\\s*['\"][^'\"]+['\"]"),
        Pattern.compile("(?i)(api_key|apikey|secret|token)\\s*=\\s*['\"][^'\"]+['\"]"),
        Pattern.compile("AKIA[0-9A-Z]{16}") // AWS Access Key pattern
    };

    @Override
    public boolean run(Path root) {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(p -> isTextFile(p))
                .filter(p -> !p.toString().contains(".git")) 
                .filter(p -> !p.toString().contains("build")) 
                .noneMatch(this::containsSecret); // If it finds a match, this returns false 
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isTextFile(Path path) {
        String name = path.toString().toLowerCase();
        return name.endsWith(".java") || name.endsWith(".kt") || name.endsWith(".xml") 
            || name.endsWith(".yml") || name.endsWith(".yaml") || name.endsWith(".properties") 
            || name.endsWith(".json") || name.endsWith(".md") || name.endsWith(".txt")
            || name.endsWith(".js") || name.endsWith(".ts") || name.endsWith(".py");
    }

    private boolean containsSecret(Path file) {
        try {
            String content = Files.readString(file);
            for (Pattern pattern : SECRET_PATTERNS) {
                if (pattern.matcher(content).find()) {
                    System.out.println("    🚨 POTENTIAL SECRET FOUND IN: " + file.getFileName());
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false; 
        }
    }
}