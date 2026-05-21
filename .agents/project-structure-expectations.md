# 📁 Project structure expectations

```yaml
.github
buildSrc/
<module-1>
  src/
  ├── main/
  │ ├── kotlin/ # Kotlin source files
  │ └── java/ # Legacy Java code
  ├── test/
  │ └── kotlin/ # Unit and integration tests
  build.gradle.kts # Kotlin-based build configuration
<module-2>
<module-3>
build.gradle.kts # Kotlin-based build configuration
settings.gradle.kts # Project structure and settings
README.md # Project overview
AGENTS.md # Entry point for LLM agent instructions
version.gradle.kts # Declares the project version in versioned Gradle Build Tools repos.
```
