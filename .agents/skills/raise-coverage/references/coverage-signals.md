# Coverage signals — localization & verification

Mechanical reference for the `raise-coverage` skill. The `SKILL.md` says *what to
do*; this file says *how to read the numbers*.

Coverage is computed by the **JaCoCo engine**, but the Spine convention is to
expose it through **Kover** with `useJacoco(version = Jacoco.version)`. Kover
owns the Gradle tasks; JaCoCo owns the engine and the XML format. The skill's
Step 0 ensures every target repo is on Kover before any analysis runs (see
[`migrate-to-kover.md`](migrate-to-kover.md)).

## Where the report lives

Kover is applied per module via the distributed `jvm-module` /
`kmp-module` script plugins, or directly:

```kotlin
plugins { /* … */ id("org.jetbrains.kotlinx.kover") }
kover {
    useJacoco(version = Jacoco.version)   // compute coverage with the JaCoCo engine
    reports.total.xml.onCheck = true       // emit XML on `check`
}
```

`useJacoco(...)` is a **Kover** DSL call — the tasks are Kover's, but the
engine and the XML format are JaCoCo's.

- Per-module report task: `:<module>:koverXmlReport`
- XML path: `<module>/build/reports/kover/report.xml`
- Same task on KMP modules configured by Spine's `kmp-module` script
  plugin — it only sets up the `total` report, so `koverXmlReport` exists
  but no `koverXmlReport<Variant>` does (a `Jvm`-suffixed task would only
  appear if a named `variant("jvm") { … }` block were declared).
- Root-level aggregation (when the repo wires it):
  `./gradlew koverXmlReport` → `build/reports/kover/report.xml`

If unsure of the output path:

```bash
find <module>/build -name '*.xml' -path '*kover*'
```

## Generating a report

```bash
# Kover — runs the module tests, then writes report.xml. Same task name
# for Kotlin-JVM and Spine `kmp-module` modules.
./gradlew :<module>:koverXmlReport
```

## Reading the XML

Kover emits the JaCoCo XML structure: `report > package > class > method`, each
with `<counter>` elements, plus `<sourcefile>` elements carrying per-line data.

- `<counter type="INSTRUCTION|BRANCH|LINE|METHOD|CLASS|COMPLEXITY"
  missed="N" covered="M"/>` — totals at each level.
- `<line nr="L" mi="missedInstr" ci="coveredInstr" mb="missedBranches"
  cb="coveredBranches"/>` — per source line (inside `<sourcefile>`).

### Gap rules

- **Uncovered line**: `ci == 0` (and `mi > 0`).
- **Partially covered branch**: `mb > 0` (regardless of `cb`).
- **High-value targets**: methods whose `BRANCH` (or `LINE`) counter has
  `missed > 0` — enumerate these first in the `SKILL.md` step-4 list.

### Non-actionable gaps (recognize and skip)

Some lines show as uncovered but cannot gain coverage from *any* test — do not
propose tests for them; report them as non-actionable:

- **Kotlin `inline` / `inline reified` functions.** The compiler inlines the body
  into every call site, so the engine credits the caller, not the definition. The
  definition lines stay `ci=0` even when fully exercised. (Verified on
  `base-libraries`: `parse<T>(...)` reified overloads remained `ci=0` after a
  passing round-trip test.)
- **Unreachable guards.** `require` / `check` / `error` branches the public API
  cannot trigger (e.g. an invariant guaranteed by construction) — the gap is real
  but unclosable from outside.
- **`throw helper(...)` where `helper` always throws.** Spine's `Exceptions`
  utilities (`newIllegalStateException`, `newIllegalArgumentException`, …) are
  *declared* to return an exception but actually throw it internally. Callers
  still write `throw newIllegalStateException(...)` to satisfy the compiler's
  flow analysis, but control never returns to the caller's `ATHROW`. JaCoCo
  attributes coverage at the line's downstream probe, which is never hit, so
  the whole line shows `mi=N ci=0` even when a test exercises the catch block
  and asserts on the exception's message. (Verified on `base-libraries`:
  `AbstractSourceFile.java:69` and `:82` remained `mi=10 ci=0` after passing
  tests that drove the `IOException` paths in `load()` and `store()` and
  asserted on the wrapped message.)

### Extracting gaps for a class

The XML carries a `DOCTYPE` pointing at a public DTD, so always pass `--nonet` to
`xmllint` (or use the Python recipe) — parsing must never reach the network. The
report has no XML namespace, so the XPath is plain.

Note that JaCoCo (and therefore Kover with `useJacoco(...)`) puts the
`<line>` elements under `<sourcefile>`, not under `<class>` — the `<class>`
element only carries summary `<counter>`s. To get the uncovered-line gaps,
query by the `<sourcefile>` that holds the class's source, scoped to the
class's package:

```bash
# Package of the FQN with '/' as the separator; source file is the simple
# class name plus the language suffix (.java or .kt).
xmllint --nonet \
  --xpath '//package[@name="io/spine/foo"]/sourcefile[@name="MyType.java"]/line[@ci="0" or @mb > 0]' \
  <module>/build/reports/kover/report.xml
```

To confirm a method-level branch gap inside that class, query the `<class>`
element's `<method>` counters:

```bash
xmllint --nonet \
  --xpath '//class[@name="io/spine/foo/MyType"]/method[counter[@type="BRANCH" and @missed>0]]/@name' \
  <module>/build/reports/kover/report.xml
```

Python (robust for large reports; reads both class/method counters and
sourcefile lines):

```python
import xml.etree.ElementTree as ET
root = ET.parse("report.xml").getroot()
for pkg in root.findall("package"):
    for sf in pkg.findall("sourcefile"):
        gaps = [l.get("nr") for l in sf.findall("line")
                if l.get("ci") == "0" or int(l.get("mb", "0")) > 0]
        if gaps:
            print(pkg.get("name"), sf.get("name"), gaps)
```

## What is in scope

Only human-written `src/main` code. Two filters already exclude the rest — honor
both, and never count an excluded file as a gap:

- **Kover filters** — `kover { filters { excludes { … } } }` drops classes by
  pattern. Generated paths (anything containing `generated`, including Protobuf
  and `protoc-gen-kotlin` output) are excluded by convention.
- **`.codecov.yml`** — `ignore` removes `**/generated/**`, `**/examples/**`,
  `**/test/**`; coverage status applies only to `src/main/**`.

## KMP / Kotlin-JVM modules

For both Kotlin-JVM and KMP modules configured by Spine's `kmp-module` script
plugin, `koverXmlReport` is the single report task — Kover only generates
`koverXmlReport<Variant>` tasks when a named `variant("…") { … }` block is
declared, and `kmp-module` declares none (it configures only the `total`
report). Add tests under the module's test source set (`src/test`, or
`src/jvmTest` / `src/commonTest` for KMP) to match.

## Verification (SKILL.md step 6)

After generating tests, re-run `:<module>:koverXmlReport` and re-parse the XML
for the targeted class: the previously listed `nr` values should no longer be
gaps, and the method/class `BRANCH` + `LINE` counters should show `missed`
reduced. Cross-check the module total against the relevant `.codecov.yml`
`project` target so nothing regresses.

---

## Appendix — future: Codecov triage tier (deferred)

v1 is local-report-only. A later iteration may add a Codecov triage tier to pick
targets across repos without a local build. If added:

- Base `https://api.codecov.io/api/v2`; `service = github`,
  `owner = SpineEventEngine`, `repo = <name>`; auth header
  `Authorization: Bearer $CODECOV_API_TOKEN`.
- Useful endpoints: per-file `totals`, line-by-line `report`, single
  `file_report`, and `commits` (for trend). Filters: `path`, `flag`,
  `component_id`.
- Read the per-line hit/miss/partial encoding from live JSON once — do not
  hardcode it; it is easy to get backwards.
- Always degrade gracefully to the local report (above) when the token is absent.

Until that lands, do everything from the local Kover report.
