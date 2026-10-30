# AGENTS Guide

JAuswertung is a Swing desktop application for running lifesaving competitions (primarily DLRG): registrations,
heats, time entry, scoring, checks, printing, import/export. It is a Maven multi-module project (Java 25, root
`pom.xml`, groupId `de.dennisfabri.jauswertung`); all code lives under `de.df.jauswertung.*`. Packages are split across
modules (e.g. `de.df.jauswertung.gui.plugins` spans every plugin module); there is no JPMS.

## Desktop, Not a Backend

JAuswertung is a single-user desktop tool. A competition is loaded completely into memory and the application works on
that object graph until it is saved. Do not import web/backend patterns that solve problems this application does not
have:

- The in-memory model (`AWettkampf` and its graph in `jauswertung-data`) is the source of truth at runtime. Files are
  read and written as a whole on open, save, and autosave — there is no database, no partial loading, no transactions,
  no pagination, no optimistic locking.
- Working on the full competition in memory is intended. Prefer clear, simple in-memory algorithms over caching or
  lazy-loading schemes; competitions are bounded (hundreds to a few thousand participants).
- The real concurrency risk is threads, not users: the model may be read and changed from the EDT and from background
  work (e.g. imports), so make sure two threads never work on the same part of the model at the same time (see
  *Swing Threading*).
- The file format is a long-lived contract with users' existing files (see *Persistence and Compatibility*).

## Modules

Listed bottom-up; the order of `<modules>` in the root `pom.xml` follows this layering.

| Module | Purpose |
| --- | --- |
| `jauswertung-files` | No Java. Runtime resource tree: `plugins/`, `plugins-mmm/` and `plugins-veranstaltung/` (plugin descriptors), `aks/` (`.rwe`/`.rwm` rulebooks), `penalties/`, icons, images, `logback.xml`, `development/`. **This is the runtime working directory.** |
| `jauswertung-data` | Domain model `de.df.jauswertung.daten` (`AWettkampf`, `EinzelWettkampf`/`MannschaftWettkampf`, `ASchwimmer`, `Laufliste`, `regelwerk.*`), `util.Utils`, `gui.util.I18n`. Lowest level. |
| `jauswertung-swing` | Shared Swing utilities, `util.DefaultInit` (application init). |
| `jauswertung-print` | Printing infrastructure (flying-saucer, certificate page `UrkundenSeite`). |
| `jauswertung-io` | Load/save (`InputManager`, `OutputManager`, `IOUtils`), CSV/Excel/HTML/PDF/XML/JSON import/export, portal model. Holds almost all tests. |
| `jauswertung-times-extractor`, `-ares`, `-dp-extensions`, `-web` | Integrations: time extraction, Ares timing, display tool/splitter, result upload. |
| `jauswertung-akeditor` | Rulebook/age-group editor (`JAKsEditor`). |
| `jauswertung-plugins-*` | GUI plugins. `-core` (`CorePlugin`: holds the current competition, load/save) and `-editor` are the base most others build on; `-input` (time entry, electronic timing, referees, …), `-print` (all printers), `-heats`, `-checks`, `-importexport` (wizards, autosave), `-undoredo`, etc. |
| `jauswertung-veranstaltungswertung` | Event-wide scoring app (`.vs` files, uses `plugins-veranstaltung/`). |
| `jteams` | Team-member registration app (uses `plugins-mmm/`). |
| `jauswertung` | Aggregator: entry points (`gui.JAuswertungLauncher`, `gui.Launcher` file-association dispatcher, `AresLauncher`, …) and the German user manual (`src/site/markdown`). |
| `jauswertung-misc` | Developer tools with their own `main` methods (test-data generation, anonymizer, records updater). Tests skipped. |
| `jauswertung-meldeunterlagen` | Registration `.xlsx` templates. |
| `jauswertung-setup` | install4j installer; only built with profile `setup`. |

### Dependency Rules

- `data` → `swing` → `print` → `io` → integrations → `plugins-core` → `plugins-editor` → other plugins → `jteams` /
  `jauswertung` → `misc` / `setup`.
- Lower modules never depend on plugin or GUI-aggregator modules. The domain model in `jauswertung-data` must not depend
  on Swing panels, plugins, or I/O formats.
- Avoid cyclic dependencies; when two modules need to share something, move it down to the lowest module that both
  already depend on.

## Plugin Architecture

The plugin framework is `jutils-plugin` in the sibling project `jutils-legacy` (`de.df.jutils.plugin`: `IPlugin`,
`APlugin`, `AFeature`, `PluginManager`, `UpdateEvent`).

- Each plugin has an XML descriptor **without file extension** in `jauswertung-files/src/main/resources/plugins/`
  (and `plugins-mmm/` if jteams needs it; the Veranstaltungswertung has its own set in `plugins-veranstaltung/`), e.g.
  `<plugin name="Printplugin" id="de.df.jauswertung.print" classname="de.df.jauswertung.gui.plugins.print.PPrintPlugin"><dependency id="de.df.jauswertung.core"/></plugin>`.
- `PluginManager` scans the plugin directory of the application (`user.dir/plugins` for JAuswertung), sorts plugins
  topologically by declared dependencies, and instantiates the classes from the classpath.
- Adding a plugin means: the class, its descriptor, a `<dependency>` element for every plugin it looks up, and a Maven
  dependency on its module in `jauswertung/pom.xml` (and `jteams/pom.xml` if applicable). Missing any of these fails
  only at runtime.
- Plugins look each other up in `setController`, e.g. `(CorePlugin) controller.getFeature("de.df.jauswertung.core", uid)`.
- Model changes are propagated with `sendDataUpdateEvent(text, reasonBits)`; reason bits are defined in
  `jauswertung-plugins-core/.../gui/UpdateEventConstants.java`. Send the narrowest correct reason bits — listeners use
  them to decide what to refresh.
- Class-name prefixes follow the existing convention (`M…` menu, `P…` panel, `O…` options); match it for new plugins.

## Swing Threading

- Swing components are only accessed on the EDT. Use `EDTUtils` (jutils-swing) or `SwingUtilities.invokeLater` to get
  there; follow what the surrounding code uses.
- The model is not bound to the EDT. Much of it is changed there today, but background work such as imports may change
  it too. Do not assume a model change happens on the EDT, and do not add code that relies on it.
- Long-running work (building printables, imports, exports, checks) runs off the EDT — `SwingWorker` or the jutils
  helpers — and must not touch Swing components.
- When background work reads or changes the model, prevent concurrent edits to the same data for its duration (e.g.
  keep the parent window disabled, as `PrintExecutor` does), and update the UI afterwards on the EDT.
- Printing: call `PrintExecutor.print(...)` / `PrintExecutor.preview(...)` (jutils-print) on the EDT. It builds the
  `Printable` in the background. Read widget state on the EDT first and pass it into the creator; never read Swing
  components inside the lambda:
  ```java
  final boolean big = bigPrint.isSelected();
  PrintExecutor.print(() -> getPrintable(big), getName(), true, controller.getWindow());
  ```
- The diagnostic "Zugriffe auf Swing außerhalb des EDT melden" (Optionen → Entwickler-Optionen, `BetaFeature.EDT_CHECKS`)
  enables `CheckThreadViolationRepaintManager` after a restart; use it when touching threading code.

## Persistence and Compatibility

- `.wk` (competition), `.wkmm` (team members), `.rwe`/`.rwm` (rulebooks), `.def` (penalties), `.vs` (event scoring) are
  ZIP files with a single `data.xml` entry, written by `OutputManager` and read by `InputManager`.
- Serialization is XStream via `IOUtils.getXStream()` (`jauswertung-io`), with an `allowTypes` whitelist and
  `aliasType` entries for old package names. Versioned classes use commons.xmt `VersionedDocument` with private
  `migrateN(...)` methods (e.g. in `AWettkampf`, `Regelwerk`, `Altersklasse`).
- **Users open files from previous years and previous versions. Every change to a serialized class must keep older
  files loadable.** Renaming or moving a class or field in `de.df.jauswertung.daten` requires an alias or a new migrate
  step; never edit an existing `migrateN`. New types that end up in a file must be covered by the `allowTypes`
  whitelist.
- When changing the model, prove compatibility with a test that loads an existing file from
  `jauswertung-io/src/test/resources` (or a fixture created with the previous version).
- `jauswertung-io/src/test/resources/compatibility/<year>/` holds one set of competitions per year with their CSV
  snapshots, created by `./generate-compatibility.sh <year>` from the last commit on `main` before March 1 of that
  year (generator: `jauswertung-misc/.../misc/ErzeugeKompatibilitaetsWettkaempfe`; 2026 is an exception, generated in
  October). `CompatibilityTest` checks them. Never edit or regenerate an existing year — a failing old year means the
  model change is incompatible. When new data structures become reachable through the UI, extend the generator so the
  next year's files contain them.
- Autosave (`MImportExportPlugin.autosave()`) and undo (serialized snapshots) reuse the same serialization — changes to
  it affect both.

## Validation

There is no server behind the UI, so the desktop application is the only line of defense. Validate where data enters:

- In the UI, before applying input to the model — surface problems immediately, at the field.
- At every import boundary: Excel/CSV registrations, timing data (Ares, AlphaServer, AlphaTiming, times extractor),
  portal/upload data, and files from disk. Treat their content as untrusted and reject or report invalid data instead
  of letting it into the model.
- Plausibility checks of a complete competition (missing times, implausible values) belong in `jauswertung-plugins-checks`.

## Internationalisation

- UI text is German and lives in `jauswertung-data/src/main/resources/jauswertung.properties` (plus
  `disziplinen.properties`/`aks.properties` in `jauswertung-io`). Properties files are UTF-8 without `\uXXXX` escapes;
  write umlauts directly.
- Look up strings with `I18n.get(key, args…)`, tooltips with `I18n.getToolTip(key)` (key `ToolTip.<key>`), shortcuts
  with `I18n.getKeyStroke(key)`. Missing keys silently fall back to the key itself, so check every new key exists.
- Never hardcode user-visible text in Java code.

## Documentation and Issues

- `docs/` holds user-facing documents (pptx/docx); the user manual is in `jauswertung/src/site/markdown`.
- `docs/issues/` tracks open findings from code work. Each open issue is a self-contained file listed in
  `docs/issues/README.md`'s table. Once an issue is fixed and the fix is verified, delete its file and remove its row
  from the table — do not mark it "Fixed" and leave the file in place, and do not fold a summary of the fix into
  `README.md` either. The commit that fixes the issue is the record; git history has it. Keep
  `docs/issues/README.md` even when its table is empty.
- When you find a problem outside the scope of the current task, add an issue there instead of fixing it in the same
  change.
- `docs/chores/` tracks planned clean-ups of code or data model that are not bugs, with the same rules: one file per
  chore, listed in `docs/chores/README.md`, deleted once done.
- `docs/reviews/README.md` logs the latest structured review per module/folder and review type (skill
  `.agents/skills/structured-review/`); its findings go to `docs/issues/`, `docs/chores/` or `docs/duplication/`.
- Every row in both tables has a severity (High/Medium/Low) and a size (S/M/L) as defined in the respective
  `README.md`; keep the rows ordered by severity, then by size. Rate new entries when you add them.

## Build, Test, Run

- Sibling dependencies (uistate, swingfx, swingbits, commons.xmt, l2fprod-common, filechooser, jutils-legacy,
  AlphaServer, alphatimer) are built and installed to `~/.m2` by `../build-dependencies.sh` (extra args such as
  `-DskipTests` are passed to Maven). Rebuild `jutils-legacy` first whenever a change here relies on a change there.
- Electronic timing reads its heats through `alphatimer-legacy-api` (`../alphatimer`: `Heat`, `LegacySerialization`,
  `AresResultFile`); the installer bundles AlphaTiming from `alphatimer-distribution` into its own `alphatiming/`
  folder with its own class path. AlphaServer is still bundled and started through `Launcher -as`.
- Do not call `mvn install` on this project.
- Fast compile check: `mvn clean compile -DskipTests`
- Full build with tests: `mvn clean verify`. When finishing a branch, a call to `mvn verify` is required.
- Single module: `mvn -pl <module> -am test` (e.g. `mvn -pl jauswertung-io -am test`)
- Single test class / method: `mvn -pl <module> -am test -Dtest=ClassName` / `-Dtest=ClassName#methodName`
  (add `-Dsurefire.failIfNoSpecifiedTests=false` when `-am` pulls in modules without that test)
- Installer: `mvn -Psetup package` (needs install4j).
- Mutation testing (PIT, profile `mutation`): `mvn -Pmutation -pl <module> test-compile
  org.pitest:pitest-maven:mutationCoverage -DtargetClasses=<classes> -DtargetTests=<tests>`; the report is in
  `<module>/target/pit-reports/`. To mutate classes of a lower module with the tests of a higher one, list both modules
  (and those in between) with `-pl` and add `-DcrossModule=true`.
- Version updates: `versions-maven-plugin` with `version-rules.xml`.
- Run: main class `de.df.jauswertung.gui.JAuswertungLauncher` from module `jauswertung`, **working directory
  `jauswertung-files/src/main/resources`** (plugins and images are resolved relative to `user.dir`), with the VM options
  from `jauswertung-setup/src/global.vmoptions`. Open a file directly with `-open <file>.wk`.
- The compiler plugin replaces `default-compile`/`default-testCompile` with the executions `java-compile` /
  `java-test-compile`; Lombok runs as an annotation processor there. Keep that in mind when editing compiler config.
- Sample competitions for manual testing are in `data/` (gitignored); old rulebooks in `archive/`.

## Tests

- JUnit 6 (Jupiter) plus Mockito. Name test classes `*Test`; there are no integration-test (`*IT`) or failsafe
  conventions.
- Most tests are result tests in `jauswertung-io/.../test/results/<competition>/`, comparing computed results against
  `src/test/resources/competitions/<competition>/`. The older ones (`dm1999`, `dm00`–`dm08`, `dsm06`) are one class per
  age group with one method per row; prefer `@ParameterizedTest` for new ones.
- Tests resolve fixtures relative to the module directory (`src/test/resources/...`); keep it that way.
- Prefer tests on the model and I/O layers (`jauswertung-data`, `jauswertung-io`); keep scoring, heat, and ranking logic
  out of Swing classes so it can be tested without a GUI.

## Coding Standards

- **Comments**: do not write a comment unless it provides benefit on its own — standing alone, with no other context,
  it must tell the reader something they could not already get from the code around it (names, types, structure, a
  nearby signature). A comment that restates what the code does, restates a parameter's name/type/default in prose, or
  explains something inferable in a few seconds of reading does not clear this bar — do not write it. The only comments
  that clear it: a hidden constraint, a subtle invariant, or a workaround for a specific bug that isn't obvious from the
  code itself. When in doubt, don't add the comment. This is a hard rule, not a default that yields to convenience or to
  matching a file's existing style — a comment already present in a file is not evidence it belongs there. Whenever you
  touch a file or area of code, also remove any existing comment in that area that fails this test; do not leave it in
  place because it predates this rule or because removing it feels out of scope.
- **PTDD (Predictive TDD)**: follow Red-Green-Refactor, but make two predictions explicit. Before writing a test, state
  precisely what the code should do and how you'll observe it (e.g. Given-When-Then). Before running a newly written or
  adjusted test, predict exactly how it will fail; if the actual failure differs, or the test passes unexpectedly, stop
  and investigate before writing any implementation — the mismatch means a wrong assumption, a broken test setup, or a
  missing dependency. Once the failure matches the prediction, implement the minimal change that turns it green, then
  tighten assertions in small increments across further red-green cycles. Source: https://ted.dev/tag/ptdd/.
- **Working approach**: keep changes localized to the requested feature or defect — no unrelated refactorings in the
  same change. Follow the dominant pattern already present in the code being touched. Preserve file formats, plugin
  ids, and public APIs used by other modules or sibling projects unless the task explicitly requires changing them.
  When in doubt, make the smallest change that satisfies the requirement — but escalate to the stricter rule when the
  concern is file compatibility, data integrity of a running competition, or correctness of results.
- **Language**: identifiers in the domain and GUI follow the existing German vocabulary (`Wettkampf`, `Schwimmer`,
  `Laufliste`, `Altersklasse`, `Strafe`); reuse those terms instead of introducing English synonyms for the same concept.

## Git Workflow

- While developing a feature, work on a feature branch and stay on it until the feature is finished. Do not merge into
  `main` in between. When the feature needs changes in a sibling library, build against that library's feature branch
  installed into `~/.m2` (see the workspace guide), not against an early merge of it.
- Start a branch before the first commit when `main` is checked out. Follow-up work goes on the branch that is checked
  out; do not create further branches unasked.
- Merge into `main` only when the feature is finished and the user asks for it, with a real merge commit
  (`git merge --no-ff`), never a fast-forward.
- **Commit messages**: write them in German, matching the existing history. The body is a brief bullet list of at most
  3–4 one-line bullets. Focus on intention — why the change was made and what behavior it affects — not mechanics
  visible in the diff. Don't mention tests that accompany a code change; do state it when a commit only adds or changes
  tests.

## Class Design

- One primary responsibility per class; split classes when responsibilities drift (e.g. a panel that also computes
  rankings and writes files). Keep domain logic out of Swing components and plugins.
- Prefer composition over large utility or god classes; keep methods short and intention-revealing.
- Use records for new immutable value and DTO types. Lombok is available (`@Slf4j`, `@Value`, `@Getter`,
  `@RequiredArgsConstructor`); prefer constructor injection of collaborators.
- Do not introduce new static mutable state or manual singletons (`getInstance()`, static instance holders); pass
  collaborators explicitly or obtain them through the plugin controller.

## Exceptions

- Throw specific exceptions named after the concept and the problem rather than generic `RuntimeException`.
- Do not swallow exceptions or end with `printStackTrace()`. Either handle the failure — and tell the user in the UI
  when an action they triggered failed — or propagate it to the code that can.
- Wrap only when the abstraction level changes (e.g. a parse error in an import becomes an import error with the row).

## Logging

- SLF4J with Logback (`jauswertung-files/src/main/resources/logback.xml`); `System.out`/`System.err` are redirected to
  SLF4J by `DefaultInit`. Use Lombok's `@Slf4j` in new code; do not add `System.out.println` or `printStackTrace()`.
  Removing the existing calls is tracked in `docs/chores/replace-system-out-with-logging.md` and
  `docs/chores/replace-printstacktrace.md`.
- `DEBUG` for diagnostic detail, `INFO` sparingly for lifecycle events (start, file opened/saved), `WARN` for handled
  unexpected situations, `ERROR` for real failures.
- Competition files contain personal data (names, birth years, clubs). Do not log participant data or whole model
  objects; log ids, file names, and counts instead. Never log credentials or tokens (portal/upload access).
- Log a stack trace once, at the place that handles or reports the failure.
