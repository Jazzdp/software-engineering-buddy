# Software Engineering Buddy 
```text
   _____      ____            __    __     
  / ___/___  / __ )__  ______/ /___/ /_  __
  \__ \/ _ \/ __  / / / / __  / __  / / / /
 ___/ /  __/ /_/ / /_/ / /_/ / /_/ / /_/ / 
/____/\___/_____/\__,_/\__,_/\__,_/\__, /  
                                  /____/
```
**Your SDLC & DevSecOps Project Coach.** Stop guessing what to do next.

[![Download CLI](https://img.shields.io/badge/Download-CLI-brightgreen)](https://github.com/Jazzdp/software-engineering-buddy/releases) 
[![Live Demo](https://img.shields.io/badge/Live-Dashboard-blue)](https://software-engineering-buddy.vercel.app/)

`sebuddy` is a CLI tool that guides you through the entire Software Development Life Cycle (SDLC). Whether you are starting a brand new project or have an existing codebase that needs a health check, this tool tells you exactly what is missing, what to do next, and gives you the tools to fix it.

## Features

- **5-Stage Roadmap:** Planning, Setup, Code Quality, DevSecOps, and Deployment.
- **Health Score:** Get a 0-100 score based on your repo's best practices.
- **Auto-Fixer:** Missing a CI pipeline or Dockerfile? One command generates it.
- **Secret Scanner:** Automatically detects hardcoded passwords and API keys.
- **Environment Check:** Run `sebuddy doctor` to check if Java, Git, and Docker are installed.
- **Visual Dashboard:** Export your report with `--json` and upload it to the [Live Web Demo](https://software-engineering-buddy.vercel.app/).

## Installation

### Windows (Recommended)
1. Go to the [Releases](https://github.com/Jazzdp/software-engineering-buddy/releases) page.
2. Download `sebuddy-installer.exe`.
3. Run the installer (choose System or User install).
4. Open a **new terminal** and run: `sebuddy check .`

### Mac / Linux
1. Download `software-engineering-buddy.jar` from the [Releases](https://github.com/Jazzdp/software-engineering-buddy/releases) page.
2. Ensure you have **Java 21** installed.
3. Run: `java -jar software-engineering-buddy.jar check .`

## Usage

Once installed, you can run the tool from any folder.

```bash
# Check your repo's health and see your full roadmap
sebuddy check .

# See exactly what to do next (focuses on the earliest incomplete stage)
sebuddy next .

# Automatically fix missing files (e.g., CI pipeline, Dependabot, Dockerfile)
sebuddy fix ci-exists .
sebuddy fix dockerfile .

# Confirm a manual step (e.g., Figma prototype)
sebuddy confirm figma-prototype .

# Check your environment (Java, Git, Docker)
sebuddy doctor

# Export a JSON report for the visual dashboard
sebuddy check . --json
```

## Related Work

Unlike static linters (like ESLint) or security scanners (like OpenSSF Scorecard) that only tell you what is wrong, `sebuddy` acts as a workflow coach. It knows the order of operations in software engineering and only tells you what to focus on in your *current* stage. It doesn't just report problems—it generates the missing files for you.

## License
MIT

