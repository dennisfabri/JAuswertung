# Review Types

Seven review lenses, applied to a resolved scope (Step 2 in SKILL.md). Not every type applies to every module — the
*Module classification* table lists which types apply to which class; anything else is N/A (Step 1: if the user picks
an N/A type, say so and ask for a different one — don't invent findings for a lens the module has no documented
basis for).

"Filtered Standards-axis pass" means: follow the `review` skill's own process with the adjustments in SKILL.md Step 4,
narrowing the Standards sub-agent's brief to only the named `AGENTS.md` sections — it should ignore standards
violations outside that lens even if visible in the diff. All section names refer to the root `AGENTS.md`.

## Module classification

The classes follow `AGENTS.md` → *Dependency Rules*. For a `folder` target, use the class of the module it lies in.

| Class | Modules | Applicable types |
| --- | --- | --- |
| model | `jauswertung-data` | Architecture, Clean code, Tests, Test coverage, Internationalisation, File compatibility |
| infrastructure | `jauswertung-swing`, `jauswertung-print`, `jauswertung-io` | all seven (File compatibility: `io` only) |
| integration | `jauswertung-ares`, `jauswertung-times-extractor`, `jauswertung-dp-extensions`, `jauswertung-web` | Architecture, Clean code, Tests, Test coverage, Threading, Internationalisation |
| plugin | `jauswertung-plugins-*` | Architecture, Clean code, Tests, Test coverage, Threading, Internationalisation |
| app | `jauswertung-akeditor`, `jauswertung-veranstaltungswertung`, `jteams`, `jauswertung` | Architecture, Clean code, Tests, Test coverage, Threading, Internationalisation |
| tooling | `jauswertung-misc` | Architecture, Clean code |
| non-Java | `jauswertung-files`, `jauswertung-meldeunterlagen`, `jauswertung-setup` | `jauswertung-files`: Architecture (plugin descriptors only); the others: none |

N/A reasons:

- **Tests / Test coverage** for modules without `src/test` (most plugins, `jteams`, `jauswertung`, `akeditor`,
  `dp-extensions`): there is nothing to measure. Report "no tests in `<module>`" instead of running a tool; whether
  logic should move into a testable lower module is an Architecture or Tests finding of a module that has tests.
  `jauswertung-misc` skips its tests by configuration, so neither type applies there.
- **Threading** for `jauswertung-data`: the model has no Swing code; threading concerns about the model are reviewed
  where the model is read or changed (plugins, io).
- **File compatibility** outside `data` and `io`: no serialized classes or serialization code live there. If a
  folder elsewhere turns out to hold a serialized type, the type applies to it.
- **Internationalisation** for `jauswertung-misc`: developer tools, no user-facing UI text.
- **Performance** is not a type: `AGENTS.md` → *Desktop, Not a Backend* rejects caching and lazy-loading schemes, so
  there is no documented standard to review against. Responsiveness concerns are covered by Threading.
- **Security** is not a type.

## Types

| Type | Mechanism |
| --- | --- |
| Architecture | Filtered Standards-axis pass — *Desktop, Not a Backend*, *Modules*, *Dependency Rules*, *Packages in jauswertung-data* (for `jauswertung-data` targets), *Plugin Architecture*. Check the module's `pom.xml` dependencies against the allowed direction for its class; a dependency on a higher layer is a hard finding, cited by module and class. For plugin modules (and `jauswertung-files`), check the wiring for every plugin class in scope: descriptor in `jauswertung-files/src/main/resources/plugins/` (and `plugins-mmm/` / `plugins-veranstaltung/` where used), a `<dependency>` element for every `controller.getFeature(...)` lookup, and the module dependency in `jauswertung/pom.xml` (and `jteams/pom.xml`) — any gap is a hard finding, since it fails only at runtime. Also check the `M…`/`P…`/`O…` prefixes and that `sendDataUpdateEvent` uses the narrowest correct reason bits. There is no ArchUnit in this repo: for every Dependency Rules finding, note that the rule is only enforced by convention and recommend a chore to enforce it with ArchUnit (don't add one as part of the review). |
| Clean code / best practices | Filtered Standards-axis pass — *Coding Standards* (the comment rule is a hard rule, not a judgement call), *Class Design*, *Exceptions*, *Logging*, *Validation*. Under Logging, logging participant data, whole model objects, or credentials is a hard finding. Under Validation, data from an import boundary or a UI field reaching the model unchecked is a hard finding. Existing `System.out`/`System.err` and `printStackTrace()` calls are tracked in `docs/chores/replace-system-out-with-logging.md` and `docs/chores/replace-printstacktrace.md` — cite those chores for old calls; only calls added in the diff are new findings. |
| Tests | Filtered Standards-axis pass — *Tests*, PTDD under *Coding Standards*, and the compatibility-test duty in *Persistence and Compatibility* (a model change without a test that loads an existing file is a hard finding). Also flag scoring, heat or ranking logic added to Swing classes, where it cannot be tested without a GUI. |
| Test coverage | PIT, see *Test coverage* below. No review-skill involvement — deterministic tool run. |
| Threading | Filtered Standards-axis pass — *Swing Threading*, plus the concurrency bullet in *Desktop, Not a Backend*. Hard findings: Swing components accessed off the EDT (including widget reads inside a `PrintExecutor` creator lambda), long-running work on the EDT, and background work changing the model without preventing concurrent edits. Check `docs/issues/print-preview-reads-widgets-off-edt.md` before reporting a print-preview finding. |
| Internationalisation | Two parts: (1) run `.agents/skills/structured-review/scripts/i18n-missing-keys.sh <pathspec>` from the repo root and report its missing keys and the computed-key calls it lists for manual checking (it inspects the current tree, not the diff); (2) filtered Standards-axis pass — *Internationalisation*: hardcoded user-visible text, `\uXXXX` escapes in properties files, English synonyms for existing German UI terms, tooltips/keystrokes not looked up via `getToolTip`/`getKeyStroke`. |
| File compatibility | Filtered Standards-axis pass — *Persistence and Compatibility*. Hard findings: a renamed or moved class or field in `de.df.jauswertung.daten` without an `aliasType`/alias or a new `migrateN` step; an edited existing `migrateN`; a new type reachable from a saved file that is not covered by the `allowTypes` whitelist in `IOUtils.getXStream()`; any change to an existing `jauswertung-io/src/test/resources/compatibility/<year>/` set; a model change without a test loading an existing file. Judgement call: new data reachable through the UI that `ErzeugeKompatibilitaetsWettkaempfe` does not generate yet. Remind the reviewer that autosave and undo share this serialization. |

## Test coverage

Run from the repo root, against the current working tree:

1. If the module has no `src/test`, and it is not `data`, `swing` or `print` (whose tests live in `jauswertung-io`),
   report "no tests in `<module>`", log nothing, and stop.
2. Collect the module's own classes as `targetClasses` — packages span modules, so a package glob would also mutate
   classes of other modules once `crossModule` is on:
   ```
   find <module>/src/main/java -name '*.java' | sed -E 's#^.*/src/main/java/##; s#\.java$##; s#/#.#g' | paste -sd,
   ```
   For a `folder` target, run the `find` on that folder (its Java sources) instead.
3. Run PIT:
   - Module with its own tests:
     `mvn -Pmutation -pl <module> -am test-compile org.pitest:pitest-maven:mutationCoverage -DtargetClasses=<classes> -DtargetTests='de.df.jauswertung.*'`
   - `jauswertung-data`, `jauswertung-swing`, `jauswertung-print` (tested mostly from `jauswertung-io`): list the
     module, `jauswertung-io` and every module in between with `-pl`, and add `-DcrossModule=true`:
     `mvn -Pmutation -pl jauswertung-data,jauswertung-swing,jauswertung-print,jauswertung-io -am test-compile org.pitest:pitest-maven:mutationCoverage -DcrossModule=true -DtargetClasses=<classes> -DtargetTests='de.df.jauswertung.*'`
4. Report the mutation score and the surviving mutants (class, line, mutator) from the generated
   `<module>/target/pit-reports/index.html` (and `mutations.xml`). For large modules, group surviving mutants by
   class and list the classes with the most survivors first.

## Baseline applicability

Test coverage is a point-in-time measurement, not a diff review — skip Step 3 (baseline resolution) for this type
entirely. Run the tool against the current working tree and log the result.

All other types are diff-based and use Step 3's baseline resolution as normal. Internationalisation's script part
also runs on the current tree; its standards part uses the baseline.
