# Merging the age groups of a scoring group exists twice

Two methods merge the age groups of a scoring group (`Wertungsgruppe`) into one age group: they copy the competition,
move the swimmers of all member age groups into the first one with `setAKNummer` and copy settings of the scoring group
onto it. They differ in focus:

- `CompetitionUtils.generateWertungsgruppenwettkampf(wk, nurMehrkampf)`
  (`jauswertung-data/.../CompetitionUtils.java:463-520`) merges **all** scoring groups (or only those with "protocol
  with multi-event ranking") and removes every swimmer outside a group. It sets name, penalty = DQ, individual
  ranking, HLW individual ranking, multi-event ranking (`true`) and clears the scoring group of the merged age group.
  Used for the scoring-group sections of the full result printout, PDF export and HTTP server
  (`PrintUtils.getResultsPrintable`, `:172`), the protocol (`PrintUtils.getWertungsgruppenPrintables`, `:626`), the
  Excel export (`ASpreadsheetExporter:76`) and the CSV/Excel result tables (`DataTableUtils.results`, `:280`).
- `ResultUtils.generateEinzelwertungswettkampf(wk, wgname, removeEmpty)`
  (`jauswertung-data/.../ResultUtils.java:327-357`) merges **one** scoring group and removes every swimmer outside it,
  sets name, penalty = DQ, individual ranking and HLW individual ranking, and then splits the result into one age
  group per discipline (individual ranking). Used by the full result printout and protocol (`PrintUtils:611`),
  "results per discipline", "documents of the individual ranking" (`APerDisciplinePrinter:266`) and the medal list per
  discipline (`ListOfMedalsPerDisciplinePrinter:233`).

The individual rankings of a scoring group therefore reach the user through two constructions: the printouts above use
`generateEinzelwertungswettkampf(wk, wgname, …)`, the CSV and Excel exports merge with
`generateWertungsgruppenwettkampf` and split with `generateEinzelwertungswettkampf(merged, index, …)`
(`DataTableUtils:296`). Both take the settings from `Wertungsgruppe.applyTo`; the merge itself is still duplicated.

Also noticed: `generateWertungsgruppenwettkampf` calls `disableUpdates()`/`enableUpdates()` on the original `wk` while
it removes swimmers from the copy `wkx`.

A third construction, `ResultSelectionUtils.getResultWettkampf`, adds a copy of the group instead of merging; it uses
`Wertungsgruppe.applyTo` as well.

## Investigate

- Whether `generateEinzelwertungswettkampf(wk, wgname, …)` can be expressed as merging one group with the merge of
  `generateWertungsgruppenwettkampf` followed by the per-discipline split, and whether the extra settings of the
  latter (multi-event ranking `true`, scoring group cleared) change any individual ranking.
- Whether CSV/Excel and the printouts produce the same individual rankings of a scoring group, ideally pinned by a
  result test before changing anything.
- Whether the `disableUpdates()` on the wrong object has an effect.

Then keep one merge of a scoring group.
