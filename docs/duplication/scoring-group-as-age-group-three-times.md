# A scoring group is turned into an age group in three places

Three methods build one age group from the age groups of a scoring group (`Wertungsgruppe`), each with its own copy
of the same steps:

- `ResultSelectionUtils.getResultWettkampf(wk)` (`jauswertung-plugins-editor/.../core/ResultSelectionUtils.java`):
  result printout, printout with detailed filter, HTTP server.
- `CompetitionUtils.generateWertungsgruppenwettkampf(wk, nurMehrkampf)` (`jauswertung-data/.../CompetitionUtils.java`):
  scoring-group sections of the full result printout, PDF export and HTTP server (`PrintUtils:172`), protocol
  (`PrintUtils:626`), Excel export (`ASpreadsheetExporter:76`), CSV/Excel result tables (`DataTableUtils:280`).
- `ResultUtils.generateEinzelwertungswettkampf(wk, wgname, removeEmpty)` (`jauswertung-data/.../ResultUtils.java`):
  individual rankings of a scoring group in the full result printout and protocol (`PrintUtils:611`), "results per
  discipline", "documents of the individual ranking" (`APerDisciplinePrinter:266`) and the medal list per discipline
  (`ListOfMedalsPerDisciplinePrinter:233`).

## Shared steps

| Step | `getResultWettkampf` | `generateWertungsgruppenwettkampf` | `generateEinzelwertungswettkampf(wgname)` |
| --- | --- | --- | --- |
| Copy the competition | ✓ (plus `minimizeCompetition`) | ✓ | ✓ |
| Which groups | marked "protocol with multi-event ranking" | all, or only marked ones | the named one |
| Find the member age groups by `getWertungsgruppe()` | ✓ | ✓ (also drops age groups whose group does not exist) | ✓ |
| Template age group | **last** member | **first** member | **first** member |
| Settings of the group (`Wertungsgruppe.applyTo`) | ✓ | ✓ | ✓ |
| Multi-event ranking | `true` | `true` | unchanged |
| Swimmers | copied into an **added** age group, originals stay | **moved** into the template, others removed | **moved** into the template, others removed |
| Afterwards | `changedNow()` | – | split into one age group per discipline |

The template supplies disciplines, record values, HLW and every other setting not taken from the scoring group. If the
member age groups differ in these, the result printout (last member) and the full printout, protocol and exports
(first member) rank the same scoring group differently.

The individual rankings of a scoring group also reach the user through two of these methods: the printouts use
`generateEinzelwertungswettkampf(wk, wgname, …)`, the CSV and Excel exports merge with
`generateWertungsgruppenwettkampf` and split with `generateEinzelwertungswettkampf(merged, index, …)`
(`DataTableUtils:296`).

Also noticed: `generateWertungsgruppenwettkampf` calls `disableUpdates()`/`enableUpdates()` on the original `wk`
while it removes swimmers from the copy `wkx`; `getResultWettkampf` deep-copies the whole competition once more for
every scoring group to get the swimmers to add.

## Investigate

- Which member age group should be the template (first or last), and whether member age groups of one scoring group
  may differ in disciplines, record values or HLW at all.
- Whether the three can share one step "build the age group of a scoring group" (find members, choose the template,
  apply the settings, collect the swimmers), with the callers only deciding whether the swimmers are added as copies
  or moved, and whether the result is split per discipline.
- Whether CSV/Excel and the printouts produce the same results for a scoring group, ideally pinned by result tests
  before changing anything.
- Whether the `disableUpdates()` on the wrong object has an effect.
