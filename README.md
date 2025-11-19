[![Ubuntu build][ubuntu-build-badge]][gh-actions]
[![codecov][codecov-badge]][codecov] &nbsp;
[![license][license-badge]][license]

# Welcome to the `base-libraries` repository

This repository contains common data types and utilities used by
the Spine SDK subprojects (e.g. [core-jvm][core-java]).

## The `annotations` module

Contains annotation types used by the Spine SDK.

## The `base` module

This module is not supposed to be used directly in an end-user project because it is 
exposed as an API dependency by [`core-jvm`][core-java] modules [`spine-client`][spine-client] and
[`spine-server`][spine-server].
But if you need to, here is how you add it to your Gradle project:

```kotlin
dependencies {
    implementation("io.spine:spine-base:$version")
}
```

## The `format` module

This module contains utilities for parsing files in various types like YAML, JSON, binary Protobuf,
or Protobuf JSON. This module is used internally by Spine SDK components. 
If you need it as a direct dependency in your Gradle project, please use the following code: 

```kotlin
dependencies {
    implementation("io.spine:spine-format:$version")
}
```

## Language versions

* **Java** — [see `BuildSettings.kt`](buildSrc/src/main/kotlin/BuildSettings.kt)


* **Kotlin** — [see `Kotlin.kt`](buildSrc/src/main/kotlin/io/spine/dependency/lib/Kotlin.kt)


* `1.x` versions were assembled with Java 8.


[gh-actions]: https://github.com/SpineEventEngine/base/actions
[ubuntu-build-badge]: https://github.com/SpineEventEngine/base/actions/workflows/build-on-ubuntu.yml/badge.svg
[codecov]: https://codecov.io/gh/SpineEventEngine/base
[codecov-badge]: https://codecov.io/gh/SpineEventEngine/base/branch/master/graph/badge.svg
[license-badge]: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat
[license]: http://www.apache.org/licenses/LICENSE-2.0
[core-java]: https://github.com/SpineEventEngine/core-java 
[spine-client]: https://github.com/SpineEventEngine/core-java/tree/master/client
[spine-server]: https://github.com/SpineEventEngine/core-java/tree/master/server
