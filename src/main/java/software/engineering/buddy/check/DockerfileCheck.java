package software.engineering.buddy.check;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DockerfileCheck implements FixableCheck {
    @Override
    public boolean run(Path root) {
        Path dockerfile = root.resolve("Dockerfile");
        if (!Files.exists(dockerfile)) return false;
        
        try {
            String content = Files.readString(dockerfile).toLowerCase();
            // Pass only if it exists AND explicitly changes the user
            return content.contains("user ");
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void fix(Path root) throws IOException {
        String dockerfileContent = """
            FROM eclipse-temurin:21-jre-alpine
            WORKDIR /app
            COPY build/libs/*.jar app.jar
            
            # Security Best Practice: Do not run as root
            RUN addgroup -S appgroup && adduser -S appuser -G appgroup
            USER appuser
            
            ENTRYPOINT ["java", "-jar", "app.jar"]
            """;
        Files.writeString(root.resolve("Dockerfile"), dockerfileContent);
    }
}