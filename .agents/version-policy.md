# Version policy

When a repository has `version.gradle.kts` at the project root, it follows the
[Spine SDK Versioning policy][wiki-versioning]. The version is kept in that
file and follows [Semantic Versioning 2.0.0][semver] with Spine-specific
extensions (snapshot `NUMBER`, patch, and flavor suffixes).

For repositories with root `version.gradle.kts`, PRs without a version bump
fail CI. Repositories without that file are not versioned Gradle Build Tools
projects; their version check is not applicable, and agents must not create
`version.gradle.kts` just to satisfy `/pre-pr`.

For the bump procedure in repositories that have root `version.gradle.kts` —
version-number selection, the commit-message convention, the rebuild,
dependency-report updates, and conflict resolution — use the
[`bump-version`](skills/bump-version/SKILL.md) skill.

[semver]: https://semver.org/
[wiki-versioning]: https://github.com/SpineEventEngine/documentation/wiki/Versioning
