# Testing `equals()` and `hashCode()`

## Use `EqualsTester`

When testing the implementation of `equals()` and `hashCode()` in Java or Kotlin classes, always use Guava's `EqualsTester`.

### Correct example:
```kotlin
import com.google.common.testing.EqualsTester

// ... inside a test method
EqualsTester()
    .addEqualityGroup(obj1A, obj1B)
    .addEqualityGroup(obj2)
    .testEquals()
```

### Why:
`EqualsTester` automatically verifies:
1. Symmetry: `a.equals(b) == b.equals(a)`
2. Transitivity: `a.equals(b) && b.equals(c) => a.equals(c)`
3. Reflexivity: `a.equals(a)`
4. Inequality with `null`.
5. Consistency between `equals()` and `hashCode()`.
