# Kotlin Test Formatting

## Backticked inner classes

When using backticked descriptive names for inner classes in Kotlin tests:
1. The `@Nested` annotation must be on the same line as the `inner class` declaration.
2. The backticked class name must be on the next line.

### Correct example:
```kotlin
@Nested internal inner class
`some descriptive name` {
    // ...
}
```

### Incorrect example:
```kotlin
@Nested
internal inner class `some descriptive name` {
    // ...
}
```
