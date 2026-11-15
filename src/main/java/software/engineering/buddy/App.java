package software.engineering.buddy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import software.engineering.buddy.check.*;
import software.engineering.buddy.model.Check;
import software.engineering.buddy.model.ChecksFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "sebuddy", 
    mixinStandardHelpOptions = true, 
    version = "0.1.0", 
    description = "Your SDLC & DevSecOps Project Coach.",
    footer = "\nRun 'sebuddy <command> --help' for more information on a specific command.",
    subcommands = {
        App.CheckCommand.class,
        App.ConfirmCommand.class,
        App.NextCommand.class,
        App.FixCommand.class,
        App.DoctorCommand.class,
        App.InitCommand.class
    }
)
public class App implements Callable<Integer> {

    // ANSI Color Codes
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";
    public static final String BOLD = "\u001B[1m";

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        printHeader();
        System.out.println("Usage: sebuddy <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  check [folder]        Check the repo against the SDLC roadmap");
        System.out.println("  next [folder]         Show the single most important next step");
        System.out.println("  fix <id> [folder]     Automatically generate missing files");
        System.out.println("  confirm <id> [folder] Mark a manual task as complete");
        System.out.println("  doctor                Check your development environment");
        System.out.println("  init [folder]         Initialize sebuddy in a folder");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -h, --help            Show this help message and exit");
        System.out.println("  -V, --version         Print version information and exit");
        return 0;
    }

    public static String color(String colorCode, String text) {
        // If Windows, return plain text to prevent weird symbols
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return text;
        }
        return colorCode + text + RESET;
    }

    public static void printHeader() {
        try (InputStream is = App.class.getResourceAsStream("/banner.txt")) {
            if (is != null) {
                String banner = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                System.out.print(color(CYAN, banner));
            }
        } catch (Exception e) {
            // Ignore if banner is missing
        }
        System.out.println(color(YELLOW, "        Your SDLC & DevSecOps Coach"));
        System.out.println();
    }

    private static Map<String, AutoCheck> getRegistry() {
        Map<String, AutoCheck> registry = new HashMap<>();
        registry.put("readme-exists", new ReadmeExistsCheck());
        registry.put("gitignore-exists", new GitignoreExistsCheck());
        registry.put("license-exists", new LicenseExistsCheck());
        registry.put("tests-exist", new TestsFolderCheck());
        registry.put("ci-exists", new CiWorkflowCheck());
        registry.put("env-in-gitignore", new EnvInGitignoreCheck());
        registry.put("quality-tools", new QualityToolCheck());
        registry.put("dependabot", new DependabotCheck());
        registry.put("secret-scan", new SecretScanCheck());
        registry.put("env-example", new EnvExampleCheck());
        registry.put("dockerfile", new DockerfileCheck());
        return registry;
    }

    private static Map<String, Boolean> getConfirmations(Path folder, ObjectMapper mapper) {
        Map<String, Boolean> confirmed = new HashMap<>();
        Path sebuddyFile = folder.resolve(".sebuddy.yml");
        if (Files.exists(sebuddyFile)) {
            try {
                Map<?, ?> savedData = mapper.readValue(sebuddyFile.toFile(), Map.class);
                Map<?, ?> confirms = (Map<?, ?>) savedData.get("confirmed");
                if (confirms != null) {
                    for (Map.Entry<?, ?> entry : confirms.entrySet()) {
                        confirmed.put(entry.getKey().toString(), (Boolean) entry.getValue());
                    }
                }
            } catch (Exception ignored) {}
        }
        return confirmed;
    }

    @Command(name = "check", description = "Check a folder against the software engineering roadmap")
    public static class CheckCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "The folder to check", defaultValue = ".")
        private Path folder;

        @Option(names = {"-s", "--stage"}, description = "Only show checks for a specific stage")
        private Integer targetStage;

        @Option(names = {"--json"}, description = "Export the results to a JSON file")
        private boolean exportJson;

        @Override
        public Integer call() throws Exception {
            printHeader();
            
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            InputStream is = getClass().getResourceAsStream("/checks.yml");
            if (is == null) {
                System.out.println(color(RED, "CRITICAL ERROR: checks.yml not found in the jar!"));
                return 1;
            }
            ChecksFile checksFile = mapper.readValue(is, ChecksFile.class);
            Map<String, Boolean> confirmed = getConfirmations(folder, mapper);
            Map<String, AutoCheck> registry = getRegistry();

            System.out.println(color(BOLD, "Folder: ") + folder.toAbsolutePath());
            System.out.println();

            int total = 0;
            int passedCount = 0;
            int currentStage = 99;
            int lastPrintedStage = -1;

            for (Check check : checksFile.checks()) {
                if (targetStage != null && check.stage() != targetStage) continue;
                
                total++;
                boolean passed = false;
                if ("auto".equals(check.type())) {
                    AutoCheck autoCheck = registry.get(check.id());
                    if (autoCheck != null) passed = autoCheck.run(folder);
                } else if ("manual".equals(check.type())) {
                    passed = confirmed.getOrDefault(check.id(), false);
                }

                if (passed) {
                    passedCount++;
                } else if (check.stage() < currentStage) {
                    currentStage = check.stage();
                }

                // Print Stage Headers cleanly
                if (check.stage() != lastPrintedStage) {
                    if (lastPrintedStage != -1) System.out.println();
                    System.out.println(color(BOLD, "STAGE " + check.stage()));
                    System.out.println("--------------------------------------------------");
                    lastPrintedStage = check.stage();
                }

                String icon = passed ? color(GREEN, "[PASS]") : color(RED, "[FAIL]");
                String typeStr = "manual".equals(check.type()) ? color(YELLOW, "(Manual)") : "";
                
                System.out.printf("%-8s %-35s %s%n", icon, check.title(), typeStr);
                
                if (!passed) {
                    System.out.println("         -> " + check.advice());
                    System.out.println("            Tool: " + color(CYAN, check.tool()));
                }
            }

            int score = total == 0 ? 0 : (int) Math.round((double) passedCount / total * 100);

            System.out.println();
            System.out.println("--------------------------------------------------");
            
            // Color the score based on health
            String scoreColor = score > 80 ? GREEN : (score > 50 ? YELLOW : RED);
            System.out.println(color(BOLD, "HEALTH SCORE: ") + color(scoreColor, score + "/100") + " (" + passedCount + "/" + total + " checks passed)");
            System.out.println("--------------------------------------------------");
            
            if (currentStage != 99) {
                System.out.println(color(YELLOW, "You are currently in Stage " + currentStage + "."));
                System.out.println("Tip: Run `sebuddy next` to see exactly what to do next.");
            } else {
                System.out.println(color(GREEN, "Excellent! You have completed all checks. Your project is production-ready."));
            }
            
            if (exportJson) {
                Map<String, Object> report = new HashMap<>();
                report.put("score", score);
                report.put("stage", currentStage);
                report.put("passed", passedCount);
                report.put("total", total);
                mapper.writeValue(folder.resolve("report.json").toFile(), report);
                System.out.println(color(CYAN, "Exported report to report.json")); 
            }
            return 0;     
        }
    }

    @Command(name = "next", description = "Show the single most important next step")
    public static class NextCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "The folder to check", defaultValue = ".")
        private Path folder;

        @Override
        public Integer call() throws Exception {
            printHeader();
            
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            InputStream is = getClass().getResourceAsStream("/checks.yml");
            if (is == null) {
                System.out.println(color(RED, "CRITICAL ERROR: checks.yml not found in the jar!"));
                return 1;
            }
            ChecksFile checksFile = mapper.readValue(is, ChecksFile.class);
            Map<String, Boolean> confirmed = getConfirmations(folder, mapper);
            Map<String, AutoCheck> registry = getRegistry();

            // Find the earliest stage that has a failing check
            int earliestStage = 99;
            
            for (Check check : checksFile.checks()) {
                boolean passed = false;
                if ("auto".equals(check.type())) {
                    AutoCheck autoCheck = registry.get(check.id());
                    if (autoCheck != null) passed = autoCheck.run(folder);
                } else if ("manual".equals(check.type())) {
                    passed = confirmed.getOrDefault(check.id(), false);
                }

                if (!passed && check.stage() < earliestStage) {
                    earliestStage = check.stage();
                }
            }

            if (earliestStage == 99) {
                System.out.println(color(GREEN, "Amazing! You have completed all current checks."));
                return 0;
            }

            // Now print all failing checks for that specific stage
            System.out.println(color(BOLD, "FOCUS ON STAGE " + earliestStage));
            System.out.println("--------------------------------------------------");
            
            for (Check check : checksFile.checks()) {
                if (check.stage() != earliestStage) continue;
                
                boolean passed = false;
                if ("auto".equals(check.type())) {
                    AutoCheck autoCheck = registry.get(check.id());
                    if (autoCheck != null) passed = autoCheck.run(folder);
                } else if ("manual".equals(check.type())) {
                    passed = confirmed.getOrDefault(check.id(), false);
                }

                if (!passed) {
                    System.out.println(color(BOLD, "Task: " + check.title()));
                    System.out.println("Why:  " + check.advice());
                    System.out.println("Tool: " + color(CYAN, check.tool()));
                    
                    if ("manual".equals(check.type())) {
                        System.out.println(color(YELLOW, "  -> Run: sebuddy confirm " + check.id() + " " + folder));
                    } else if (registry.get(check.id()) instanceof FixableCheck) {
                        System.out.println(color(YELLOW, "  -> Run: sebuddy fix " + check.id() + " " + folder));
                    }
                    System.out.println();
                }
            }
            
            return 0;
        }
    }

    @Command(name = "confirm", description = "Mark a manual check as complete")
    public static class ConfirmCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "The ID of the check to confirm")
        private String id;

        @Parameters(index = "1", description = "The folder to apply this to", defaultValue = ".")
        private Path folder;

        @Override
        public Integer call() throws Exception {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Path sebuddyFile = folder.resolve(".sebuddy.yml");
            
            Map<String, Object> data = new HashMap<>();
            if (Files.exists(sebuddyFile)) {
                data = mapper.readValue(sebuddyFile.toFile(), Map.class);
            }

            Map<String, Boolean> confirmed = (Map<String, Boolean>) data.getOrDefault("confirmed", new HashMap<String, Boolean>());
            confirmed.put(id, true);
            data.put("confirmed", confirmed);

            mapper.writeValue(sebuddyFile.toFile(), data);
            System.out.println(color(GREEN, "[PASS]") + " Confirmed manual check: " + color(BOLD, id));
            System.out.println("       Saved to " + sebuddyFile.toAbsolutePath());
            return 0;
        }
    }

    @Command(name = "fix", description = "Automatically fix a missing check by generating the required files")
    public static class FixCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "The ID of the check to fix (e.g., ci-exists)")
        private String id;

        @Parameters(index = "1", description = "The folder to apply this to", defaultValue = ".")
        private Path folder;

        @Override
        public Integer call() throws Exception {
            Map<String, AutoCheck> registry = getRegistry();
            AutoCheck check = registry.get(id);

            if (check == null) {
                System.out.println(color(RED, "Unknown check ID: " + id));
                return 1;
            }

            if (check.run(folder)) {
                System.out.println(color(GREEN, "This check already passes!"));
                return 0;
            }

            if (check instanceof FixableCheck fixable) {
                fixable.fix(folder);
                System.out.println(color(GREEN, "[PASS]") + " Successfully generated files for: " + color(BOLD, id));
            } else {
                System.out.println(color(RED, "This check cannot be automatically fixed. Please do it manually."));
            }
            return 0;
        }
    }

    @Command(name = "doctor", description = "Check your development environment")
    public static class DoctorCommand implements Callable<Integer> {

        @Override
        public Integer call() {
            printHeader();
            System.out.println(color(BOLD, "ENVIRONMENT CHECK"));
            System.out.println("--------------------------------------------------");

            // 1. Check Java
            String javaVersion = System.getProperty("java.version");
            int majorVersion = Integer.parseInt(javaVersion.split("\\.")[0]);
            if (majorVersion >= 21) {
                System.out.println(color(GREEN, "[PASS]") + " Java Version: " + color(BOLD, javaVersion));
            } else {
                System.out.println(color(RED, "[FAIL]") + " Java Version: " + color(BOLD, javaVersion) + " (Java 21+ required)");
            }

            // 2. Check Git
            try {
                Process process = new ProcessBuilder("git", "--version").start();
                if (process.waitFor() == 0) {
                    System.out.println(color(GREEN, "[PASS]") + " Git is installed");
                } else {
                    System.out.println(color(RED, "[FAIL]") + " Git is not installed");
                }
            } catch (Exception e) {
                System.out.println(color(RED, "[FAIL]") + " Git is not installed");
            }

            // 3. Check Docker
            try {
                Process process = new ProcessBuilder("docker", "--version").start();
                if (process.waitFor() == 0) {
                    System.out.println(color(GREEN, "[PASS]") + " Docker is installed");
                } else {
                    System.out.println(color(YELLOW, "[WARN]") + " Docker is not installed (Optional)");
                }
            } catch (Exception e) {
                System.out.println(color(YELLOW, "[WARN]") + " Docker is not installed (Optional)");
            }

            System.out.println("--------------------------------------------------");
            return 0;
        }
    }

    @Command(name = "init", description = "Initialize sebuddy in a new folder")
    public static class InitCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "The folder to initialize", defaultValue = ".")
        private Path folder;

        @Override
        public Integer call() throws Exception {
            printHeader();
            System.out.println(color(BOLD, "INITIALIZING SEBUDDY"));
            System.out.println("--------------------------------------------------");

            // Create .sebuddy.yml
            Path configFile = folder.resolve(".sebuddy.yml");
            if (Files.exists(configFile)) {
                System.out.println(color(YELLOW, "Config file already exists at " + configFile.toAbsolutePath()));
            } else {
                String defaultConfig = "# This file tracks your manual check confirmations.\n# Do not edit manually.\nconfirmed: {}\n";
                Files.writeString(configFile, defaultConfig);
                System.out.println(color(GREEN, "[PASS]") + " Created .sebuddy.yml");
            }

            // Ensure .sebuddy.yml is in .gitignore
            Path gitignore = folder.resolve(".gitignore");
            if (Files.exists(gitignore)) {
                String content = Files.readString(gitignore);
                if (!content.contains(".sebuddy.yml")) {
                    Files.writeString(gitignore, content + "\n.sebuddy.yml\n");
                    System.out.println(color(GREEN, "[PASS]") + " Added .sebuddy.yml to .gitignore");
                }
            }

            System.out.println("--------------------------------------------------");
            System.out.println(color(GREEN, "Initialization complete! You are ready to start."));
            System.out.println("Tip: Run `sebuddy check .` to see your roadmap.");
            
            return 0;
        }
    }
}