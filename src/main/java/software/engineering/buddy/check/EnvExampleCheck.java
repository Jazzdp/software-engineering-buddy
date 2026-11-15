package software.engineering.buddy.check;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EnvExampleCheck implements FixableCheck {
    @Override
    public boolean run(Path root) {
        return Files.exists(root.resolve(".env.example")) || Files.exists(root.resolve(".env.template"));
    }

    @Override
    public void fix(Path root) throws IOException {
        String template = """
            # Copy this file to .env and fill in the values
            # DO NOT commit your actual .env file!

            API_KEY=your_api_key_here
            DATABASE_URL=postgres://user:password@localhost:5432/mydb
            """;
        Files.writeString(root.resolve(".env.example"), template);
    }
}