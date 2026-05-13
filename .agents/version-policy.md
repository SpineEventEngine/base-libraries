# Version policy

The project follows the [Spine SDK Versioning policy][wiki-versioning].
The version is kept in `version.gradle.kts` at the project root and follows
[Semantic Versioning 2.0.0][semver] with Spine-specific extensions
(snapshot `NUMBER`, patch, and flavor suffixes).

PRs without a version bump fail CI.

For the bump procedure — version-number selection, the commit-message
convention, the rebuild, dependency-report updates, and conflict resolution —
use the [`bump-version`](skills/bump-version/SKILL.md) skill.

[semver]: https://semver.org/
[wiki-versioning]: https://github.com/SpineEventEngine/documentation/wiki/Versioning
