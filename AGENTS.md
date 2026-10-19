# AGENTS.md

This file provides guidance for AI coding agents (Codex, Claude, Qwen, Llama, etc.) working in this repository.

## Project Info

* **Project**: ICBM-Classic (MinecraftForge mod)
* **Mod id / group / version**: see `src/main/resources/META-INF/mods.toml` (`modId`) and `gradle.properties` (`group`,
  `version_major`, `version_minor`, `version_revise`)
* **Build tool**: Gradle (ForgeGradle / MinecraftForge), wrapper scripts `gradlew` / `gradlew.bat`
* **Minecraft version**: see `gradle.properties` -> `version_mc`
* **Forge/plugin version**: see `build.gradle` -> `net.minecraftforge.gradle` plugin version
* **Java version**: see `build.gradle` -> `java.toolchain.languageVersion` (do not hardcode the version anywhere else;
  always read it from `build.gradle`)

Do not copy/duplicate the above values into other docs. Always point readers (human or agent) back to these reference
files so the values never go stale.

## Common Commands

* Build: `./gradlew build` (Windows: `gradlew.bat build`)
* Run tests: `./gradlew test` (JUnit 5 + Mockito, working directory `./run/tests`)
* Run client/server for manual verification: see `run/` folder and Gradle run tasks (`./gradlew runClient`,
  `./gradlew runServer`)

## Development Workflow: Test-Driven Development (TDD)

All code changes (bug fixes and new features) must follow TDD:

1. Write or update a failing test first that reproduces the bug or specifies the new behavior.
2. Run the test and confirm it fails for the expected reason.
3. Implement the minimal change required to make the test pass.
4. Run the test suite again and confirm it passes.
5. Refactor if needed, keeping tests green.

Only skip writing a test when it is genuinely impractical (e.g. trivial typo fixes, pure documentation changes); state
the reason explicitly in the work log when doing so.

## Work Logging: `.scratch/ai-work-log`

`.scratch/` is a local-only working directory for the whole project, not an agent-only folder. It is **not** committed
to the project history and is listed in `.gitignore`. Besides `ai-work-log/`, it can also hold other local-only, ad-hoc
outputs such as human developer notes, helper scripts, downloaded temp references, or test run output — anything that
shouldn't be committed. `ai-work-log/` specifically is the AI-agent work history subfolder, and exists purely to
preserve agent work history across sessions and tools.

Rules:

* Every discrete work task gets its own Markdown file in `.scratch/ai-work-log/`.
* File naming: `YYYY-MM-DD-HHMM_short-title.md`, e.g. `2026-10-18-2318_recipe_data_gen.md`.
    * The date/time prefix uses 24-hour local time, no seconds.
    * The title portion is a short, lowercase, underscore- or hyphen-separated summary of the task, 60 characters or
      fewer.
* Files are **append-only**. Never rewrite or delete existing entries in a log file; always add new information below
  the existing content (e.g. a new `## Update <timestamp>` section).
* Use the log to record: the task/issue being worked on, investigation notes, decisions made, commands run, test
  results, and the final outcome, so future agents/sessions can pick up context quickly.
