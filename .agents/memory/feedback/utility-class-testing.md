# Utility class testing

In Spine libraries, utility classes (classes with only static methods and a private constructor) should be tested using `UtilityClassTest` as a base class.

## Why

`UtilityClassTest` automatically:
1. Verifies that the class is `final`.
2. Verifies that it has exactly one private constructor.
3. Verifies that the constructor throws an exception (usually `AssertionError`) or simply that it can be instantiated via reflection (depending on the implementation of `UtilityClassTest`), thus covering the private constructor for coverage purposes.

## How

### Kotlin

Inherit from `UtilityClassTest<TargetClass>(TargetClass::class.java)`:

```kotlin
@DisplayName("`MyUtils` should")
internal class MyUtilsSpec : UtilityClassTest<MyUtils>(MyUtils::class.java) {
    // ... tests ...
}
```

### Java

Inherit from `UtilityClassTest<TargetClass>`:

```java
@DisplayName("`MyUtils` should")
class MyUtilsTest extends UtilityClassTest<MyUtils> {
    
    MyUtilsTest() {
        super(MyUtils.class);
    }
    // ... tests ...
}
```
