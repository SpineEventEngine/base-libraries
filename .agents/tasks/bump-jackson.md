# Migrate `base-libraries` to Jackson 3.2.1

Task brief: `config/docs/jackson-3-migration-brief.md` (authoritative copy supplied
by the user). Target: `tools.jackson:jackson-bom:3.2.1` — confirmed the newest
3.2.x on Maven Central (`latest`/`release` = 3.2.1, checked 2026-08-07).

The `config` repo already carries the migrated `Jackson` dependency object on its
`bump-jackson` branch (`dbfa5398`, pushed to origin). This repo consumes it via
the `config` submodule + `buildSrc` copy.

## Phase 0 inventory

### Modules declaring Jackson dependencies

Only `format` (`format/build.gradle.kts`):

- `platform(Jackson.bom)` — 2.22.1 → 3.2.1
- `implementation(databind)` — group moves to `tools.jackson.core`
- `implementation(DataFormat.yaml)` — group moves to `tools.jackson.dataformat`
- `implementation(DataType.jdk8)` — **remove**: merged into `jackson-databind` in 3.0
- `implementation(DataType.dateTime)` (jsr310) — **remove**: merged into databind
- `implementation(DataType.guava)` — stays (3.x line exists)
- `runtimeOnly(moduleKotlin)` — group moves to `tools.jackson.module`

`buildSrc` has its own decoupled `jacksonVersion = 2.18.3` (used for XML parsing
inside build logic); config@bump-jackson deliberately keeps it on 2.x — buildSrc
sources still use the `com.fasterxml.jackson.*` API.

### Sources using Jackson (all in `format`)

- `io.spine.format.JacksonSupport` — public `@SPI` base; holds
  `internal abstract val factory: JsonFactory`, protected lazy
  `mapper = ObjectMapper(factory)` (illegal in 3.x), public companion
  `modules: MutableList<Module>` seeded by `ObjectMapper.findModules()`.
- `io.spine.format.parse.JacksonParser` (+ `JsonParser`, `YamlParser` objects).
- `io.spine.format.write.JacksonWriter` (+ `JsonWriter`, `YamlWriter` objects;
  `JsonFactory()` / `YAMLFactory()` construction).

### Persistence / API boundaries

`format` is a library; it does not persist anything itself, but downstream Spine
SDK tools (compiler, tool-base, …) use `io.spine.format.write`/`parse` for
settings and interchange files (JSON/YAML). Wire format of *their* output changes
with 3.x defaults (property order, java.time representation). Guarded here by
2.22.1-generated fixtures + round-trip tests; downstream repos migrate separately
(brief §2.3) and their Jackson stays 2.x until then — formats coexist.

`ProtoBinary` / `ProtoJson` formats use protobuf-java, not Jackson — unaffected.

### Custom serializers / modules / IOException catch sites

- No custom `JsonSerializer`/`JsonDeserializer`/`Module`/modifier implementations.
- No `catch (IOException)` / `@Throws(IOException::class)` anywhere in module
  sources. Only stale KDoc `@throws java.io.IOException` claims in `Parse.kt`
  (4×) and `parse/Parser.kt` (1×) — to be corrected (3.x throws unchecked
  `JacksonException`).
- §11 removals: none in use (`DataFormatDetector`, `canSerialize`,
  `MappingJsonFactory`, jsonSchema, `ObjectCodec`, `JsonFactory.get/setCodec`).

## Plan

1. Submodule → config@bump-jackson; copy buildSrc delta (Jackson.kt,
   buildSrc/build.gradle.kts comment). Commit (mechanical).
2. Still on 2.22.1: write deterministic JSON/YAML fixtures via
   `io.spine.format.write` into `format/src/test/resources/…/v2/`. Commit.
3. Migrate `format` build deps + sources to `tools.jackson.*`, builder-based
   immutable mappers (`JsonMapper.builder()` / `YAMLMapper.builder()`),
   `Module` → `JacksonModule`, KDoc updates. Commit.
4. Add fixture round-trip tests (2.x file → 3.x parse) + capture 3.x output
   diff vs 2.x baseline for the report. Commit.
5. Verify: `./gradlew build`, residual grep (only `com.fasterxml.jackson.annotation`
   may remain), `:format:dependencies` (no unintended 2.x Jackson), Dokka.
6. Report per brief §14.

## Decisions log

- 3.x changed defaults: accepted the new defaults (no restores). The library has
  no persisted contract of its own; only mapper config was `INDENT_OUTPUT`,
  which `JacksonSupport` still enables explicitly.
  `builderWithJackson2Defaults()` scaffold skipped — single mapper construction
  site, small test surface; all tests passed on 3.x defaults directly.
- `JacksonSupport.factory` was `internal`, so it was replaced by
  `internal abstract fun mapperBuilder(): MapperBuilder<*, *>` without breaking
  external API; `modules: MutableList<Module>` is public — its element type
  change (`Module` → `JacksonModule`) is an unavoidable breaking change of this
  major migration (version already bumped on this branch).

## Results (verified 2026-08-08)

- Resolved: `jackson-bom` / `jackson-databind` / `jackson-module-kotlin` 3.2.1;
  `jackson-annotations` 2.22 (BOM-resolved, the only `com.fasterxml` artifact
  on the `:format` runtime classpath).
- Wire format: the **only** diff between 2.22.1 and 3.2.1 outputs of the fixture
  value is `java.time.Instant` — numeric epoch → ISO-8601 string
  (`DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS` now off). Property order is
  unchanged for Kotlin data classes (constructor properties keep declaration
  order); YAML output is otherwise byte-identical despite the snakeyaml-engine
  switch (`---` marker and quoting preserved).
- `Jackson2CompatibilitySpec` proves 2.x-written JSON/YAML (numeric `Instant`)
  parse intact under 3.2.1.
- `./gradlew build` and `dokkaGenerate` clean; residual
  `com.fasterxml.jackson` grep: zero hits in module sources (this repo never
  used the 2.x annotations package). No test deleted or disabled.
- IOException catch sites: none existed; 5 stale KDoc `@throws` claims fixed.
- §11 removals: none in use.
- Deferred: `buildSrc`'s own Jackson stays 2.x by design (config-owned);
  jackson-module-kotlin 3.3.x raises the Kotlin floor to 2.2 — this repo is on
  Kotlin 2.3.x already, so the next LTS bump is unblocked from that side.

## Follow-up folded into this branch: `JacksonSupport` SPI rework

Per the user's request (initially spawned as a separate task, then redirected
to this same branch):

- Manual module registration is dropped entirely (per the user's decision):
  the former `public MutableList` companion — and the interim
  `registerModule()` design — are replaced by `ServiceLoader`-only discovery.
  Each mapper builder calls `MapperBuilder.findAndAddModules()`; modules are
  contributed by exposing them as `ServiceLoader` services (e.g., via
  `@AutoService(JacksonModule::class)`). `JacksonSupport` no longer has a
  companion object; removing the published `modules` property is a breaking
  change covered by the `.430` bump. Module discovery remains covered by the
  JSON/YAML round-trip specs, which require the Kotlin and Guava modules.
- The `internal abstract mapperBuilder()` vs. `@SPI public class` tension is
  resolved on the documentation side: [Format] is a **sealed** class, so new
  formats can only be added inside the `format` module. The class KDoc now says
  so instead of implying external extension. `mapperBuilder()` stays `internal`.
- Review fixes applied from three review agents (kotlin-engineer,
  spine-code-review, review-docs): `YAMLMapper` KDoc sentence rewritten
  (meaning-distorting attachment), `Instant`/ISO-8601 wire note added to the
  `mapper` KDoc, `@throws JacksonException` extended with "or its subclass",
  widow-line reflows, `@DisplayName` backticks, `javaClass.getResource`.
- Version re-bumped `.427` → `.430`: the branch carries breaking API changes
  (`modules` type changed twice over: `MutableList<Module>` →
  `List<JacksonModule>`), and the version policy rounds breaking changes up to
  the next multiple of ten. Sanctioned re-bump: reclassification to a breaking
  PR.
