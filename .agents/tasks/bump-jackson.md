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

- 3.x changed defaults: prefer accepting new defaults (library has no persisted
  contract of its own; only mapper config was `INDENT_OUTPUT`); every accepted
  wire-format change is proven parseable-from-2.x by fixture tests and listed in
  the report. `builderWithJackson2Defaults()` scaffold skipped — single mapper
  construction site, small test surface; revisit if tests surface landmines.
- `JacksonSupport.factory` is `internal`, so its type/shape can change without
  breaking external API; `modules: MutableList<Module>` is public — its element
  type change (`Module` → `JacksonModule`) is an unavoidable breaking change of
  this major migration (version already bumped on this branch).
