package software.engineering.buddy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import software.engineering.buddy.check.*;
import software.engineering.buddy.check.DependabotCheck;
import software.engineering.buddy.check.EnvInGitignoreCheck;
import software.engineering.buddy.check.QualityToolCheck;
import software.engineering.buddy.model.Check;
import software.engineering.buddy.model.ChecksFile;
import software.engineering.buddy.check.SecretScanCheck;

@Command(
    name = "sebuddy",
    mixinStandardHelpOptions = true,
    version = "0.1",
    description = "Software Engineering Buddy",
    subcommands = {App.CheckCommand.class, App.ConfirmCommand.class, App.NextCommand.class})
public class App implements Callable<Integer> {

  public static void main(String[] args) {
    int exitCode = new CommandLine(new App()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() {
    CommandLine.usage(this, System.out);
    return 0;
  }

  @Command(name = "check", description = "Check a folder against the software engineering roadmap")
  public static class CheckCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The folder to check", defaultValue = ".")
    private Path folder;
    @picocli.CommandLine.Option(names = {"-s", "--stage"}, description = "Only show checks for a specific stage (e.g., 1, 2, 3)")
    private Integer targetStage;

    @Override
    public Integer call() throws Exception {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      InputStream is = getClass().getResourceAsStream("/checks.yml");
      ChecksFile checksFile = mapper.readValue(is, ChecksFile.class);

      // Load manual confirmations if they exist
      Map<String, Boolean> confirmed = new HashMap<>();
      Path sebuddyFile = folder.resolve(".sebuddy.yml");
      if (Files.exists(sebuddyFile)) {
        Map<?, ?> savedData = mapper.readValue(sebuddyFile.toFile(), Map.class);
        Map<?, ?> confirms = (Map<?, ?>) savedData.get("confirmed");
        if (confirms != null) {
          for (Map.Entry<?, ?> entry : confirms.entrySet()) {
            confirmed.put(entry.getKey().toString(), (Boolean) entry.getValue());
          }
        }
      }

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

      System.out.println("Checking folder: " + folder.toAbsolutePath());
      System.out.println("--------------------------------------------------");

      int currentStage = 99;
      int unfinishedInCurrentStage = 0;

      for (Check check : checksFile.checks()) {
          if (targetStage != null && check.stage() != targetStage) {
            continue; 
        }
        boolean passed = false;

        if ("auto".equals(check.type())) {
          AutoCheck autoCheck = registry.get(check.id());
          if (autoCheck != null) {
            passed = autoCheck.run(folder);
          }
        } else if ("manual".equals(check.type())) {
          passed = confirmed.getOrDefault(check.id(), false);
        }

        if (!passed && check.stage() < currentStage) {
          currentStage = check.stage();
        }

        String icon = passed ? "[PASS]" : "[FAIL]";
        String typeStr = "manual".equals(check.type()) ? "(Manual)" : "";
        System.out.printf("%-8s Stage %d: %-25s %s%n", icon, check.stage(), check.title(), typeStr);

        if (!passed) {
          System.out.println("         -> " + check.advice() + " [" + check.tool() + "]");
        }
      }

      System.out.println("--------------------------------------------------");
      if (currentStage == 99) {
        System.out.println("You have completed all checks! Your project is ready.");
      } else {
        System.out.println(
            "You are currently in Stage "
                + currentStage
                + ". Focus on completing the [FAIL] items here.");
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

      Map<String, Boolean> confirmed =
          (Map<String, Boolean>) data.getOrDefault("confirmed", new HashMap<String, Boolean>());
      confirmed.put(id, true);
      data.put("confirmed", confirmed);

      mapper.writeValue(sebuddyFile.toFile(), data);
      System.out.println("✓ Confirmed manual check: " + id);
      System.out.println("  Saved to " + sebuddyFile.toAbsolutePath());
      return 0;
    }
  }

  @Command(name = "next", description = "Show the single most important next step")
  public static class NextCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The folder to check", defaultValue = ".")
    private Path folder;

    @Override
    public Integer call() throws Exception {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      InputStream is = getClass().getResourceAsStream("/checks.yml");
      ChecksFile checksFile = mapper.readValue(is, ChecksFile.class);

      // Load manual confirmations
      Map<String, Boolean> confirmed = new HashMap<>();
      Path sebuddyFile = folder.resolve(".sebuddy.yml");
      if (Files.exists(sebuddyFile)) {
        Map<?, ?> savedData = mapper.readValue(sebuddyFile.toFile(), Map.class);
        Map<?, ?> confirms = (Map<?, ?>) savedData.get("confirmed");
        if (confirms != null) {
          for (Map.Entry<?, ?> entry : confirms.entrySet()) {
            confirmed.put(entry.getKey().toString(), (Boolean) entry.getValue());
          }
        }
      }

      Map<String, AutoCheck> registry = new HashMap<>();
      registry.put("readme-exists", new ReadmeExistsCheck());
      registry.put("gitignore-exists", new GitignoreExistsCheck());
      registry.put("license-exists", new LicenseExistsCheck());
      registry.put("tests-exist", new TestsFolderCheck());
      registry.put("ci-exists", new CiWorkflowCheck());
      registry.put("env-in-gitignore", new EnvInGitignoreCheck());
      registry.put("dependabot", new DependabotCheck());
      registry.put("quality-tools", new QualityToolCheck());
      registry.put("secret-scan", new SecretScanCheck());

      // Sort checks by stage so we process them in order
      checksFile.checks().sort((c1, c2) -> Integer.compare(c1.stage(), c2.stage()));

      for (Check check : checksFile.checks()) {
        boolean passed = false;
        if ("auto".equals(check.type())) {
          AutoCheck autoCheck = registry.get(check.id());
          if (autoCheck != null) passed = autoCheck.run(folder);
        } else if ("manual".equals(check.type())) {
          passed = confirmed.getOrDefault(check.id(), false);
        }

        // The first thing we find that fails is our "Next Step"
        if (!passed) {
          System.out.println("YOUR NEXT STEP (Stage " + check.stage() + ")");
          System.out.println("--------------------------------------------------");
          System.out.println("Task:   " + check.title());
          System.out.println("Why:    " + check.advice());
          System.out.println("Tool:   " + check.tool());

          if ("manual".equals(check.type())) {
            System.out.println("\nWhen you are done, run:");
            System.out.println("  sebuddy confirm " + check.id() + " " + folder);
          }
          return 0; // Stop after showing the first failure
        }
      }

      System.out.println("You have completed all current checks. Time to add more to checks.yml!");
      return 0;
    }
  }
}
