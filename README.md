# Software Engineering Buddy 

Your SDLC & DevSecOps Project Coach. Stop guessing what to do next.

`sebuddy` is a CLI tool that guides you through the entire Software Development Life Cycle (SDLC). Whether you are starting a brand new project, or have an existing codebase that needs a health check, this tool tells you exactly what is missing, what to do next, and gives you the tools to fix it.

## Features
- **5-Stage Roadmap:** Planning, Setup, Code Quality, DevSecOps, and Deployment.
- **Health Score:** Get a 0-100 score based on your repo's best practices.
- **Auto-Fixer:** Missing a CI pipeline? Just run `sebuddy fix ci-exists .` and it generates it for you.
- **Security Scanning:** Automatically detects hardcoded passwords and API keys.
- **Environment Check:** Run `sebuddy doctor` to check if Java, Git, and Docker are installed.

## Installation
1. Go to the [Releases](https://github.com/<YOUR_GITHUB_USERNAME>/software-engineering-buddy/releases) page.
2. Download the latest `software-engineering-buddy.jar`.
3. Make sure you have **Java 21** installed.

## Usage
Run the tool from any folder:

```bash
# Check your repo's health and see your roadmap
java -jar software-engineering-buddy.jar check .

# See exactly what to do next
java -jar software-engineering-buddy.jar next .

# Automatically fix missing files (e.g., CI pipeline or Dockerfile)
java -jar software-engineering-buddy.jar fix ci-exists .

# Confirm a manual step (e.g., Figma prototype)
java -jar software-engineering-buddy.jar confirm figma-prototype .