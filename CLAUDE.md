# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

GitLink is a JetBrains plugin that provides shortcuts to open or copy file, directory, or commit URLs to remote Git hosting platforms. It supports GitHub, Bitbucket, GitLab, Gitee, Gitea, Gogs, Azure, sourcehut, Gerrit, and custom hosts via URL templates.

## Build System & Commands

This is a Kotlin-based IntelliJ Platform plugin using Gradle with the IntelliJ Platform Gradle Plugin.

### Common Commands

**Note**: JAVA_HOME must be set correctly. If build fails with JAVA_HOME error, use:
```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
```

- **Build**: `./gradlew build`
- **Run plugin in IDE**: `./gradlew runIde`
- **Test**: `./gradlew test`
- **Verify plugin**: `./gradlew verifyPlugin`
- **Build plugin distribution**: `./gradlew buildPlugin`

### Testing
- Tests use JUnit 5 and MockK
- Test files are in `src/test/kotlin/`
- Run tests with: `./gradlew test`

## Architecture

The plugin follows a middleware pipeline pattern for URL generation:

### Core Components

1. **Context System** (`Context.kt`): Sealed class hierarchy defining different contexts:
   - `ContextCommit`: For commit URLs
   - `ContextFileAtCommit`: For file at specific commit
   - `ContextCurrentFile`: For current file state

2. **Pipeline** (`pipeline/`): Middleware-based processing pipeline that:
   - Resolves context information
   - Generates URLs based on platform
   - Handles HTTPS forcing, timing, and notifications

3. **Platform Support** (`platform/`): Each Git hosting platform has:
   - Platform definition with domains and patterns
   - URL factory for generating platform-specific URLs
   - Support for custom platforms via templates

4. **UI Actions** (`ui/actions/`): Multiple action types:
   - Menu actions (browser open, copy, markdown)
   - Gutter actions (editor margin)
   - VCS log actions (git log context menu)
   - Annotation actions (blame view)

### Key Patterns

- **URL Generation**: Uses factory pattern with `UrlFactory` implementations per platform
- **Settings**: Separate application and project-level settings
- **Extensions**: Uses IntelliJ Platform extension points for integration
- **Notifications**: Custom notification system for user feedback

### Package Structure

- `git/`: Git-related utilities and extensions
- `platform/`: Platform definitions and detection
- `pipeline/`: Processing pipeline and middleware
- `settings/`: Configuration management
- `ui/`: User interface components and actions
- `url/`: URL generation factories and templates

## Plugin Configuration

The plugin is configured via `plugin.xml` and registers:
- Multiple action groups (Git menu, editor contexts, VCS log)
- Settings configurables (project and application level)
- Startup listeners and service implementations
- Extension providers for annotations and selection targets

## Dependencies

- Requires `Git4Idea` plugin (bundled with IntelliJ)
- Uses custom URL library (`libs/url-0.0.11.jar`)
- JVM target: Java 17
- Platform: IntelliJ Community 2023.3.7+