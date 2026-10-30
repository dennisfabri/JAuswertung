# A scoring group is turned into an age group in three places

Three methods build one age group from the age groups of a scoring group (`Wertungsgruppe`), each with its own copy
of the same steps. Their results differ in what the competition contains:

- `getResultWettkampf`: the whole competition, with each marked scoring group as an **additional** age group.
- `generateWertungsgruppenwettkampf`: **every** scoring group (or every marked one) as one age group each, in one
  competition. The other age groups stay in the rulebook, empty.
- `generateEinzelwertungswettkampf(wgname)`: **one** scoring group, split into one age group per discipline.

Used by:

- `ResultSelectionUtils.getResultWettkampf(wk)` (`jauswertung-plugins-editor/.../core/ResultSelectionUtils.java`):
  result printout, printout with detailed filter, HTTP server.
- `CompetitionUtils.generateWertungsgruppenwettkampf(wk, nurMehrkampf)` (`jauswertung-data/.../CompetitionUtils.java`):
  scoring-group sections of the full result printout, PDF export and HTTP server (`PrintUtils:169`), protocol
  (`PrintUtils:514`), Excel export (`ASpreadsheetExporter:76`), CSV/Excel result tables (`DataTableUtils:280`).
- `ResultUtils.generateEinzelwertungswettkampf(wk, wgname, removeEmpty)` (`jauswertung-data/.../ResultUtils.java`):
  individual rankings of a scoring group in the full result printout and protocol (`PrintUtils:499`), "results per
  discipline", "documents of the individual ranking" (`APerDisciplinePrinter:268`) and the medal list per discipline
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
| Swimmers | copied into an **added** age group, originals stay | **moved** into the template; age groups without a group emptied | **moved** into the template; all other age groups emptied |
| Afterwards | `changedNow()` | – | split into one age group per discipline |

The template supplies disciplines, record values, HLW and every other setting not taken from the scoring group. If the
member age groups differ in these, the result printout (last member) and the full printout, protocol and exports
(first member) rank the same scoring group differently.

The individual rankings of a scoring group also reach the user through two of these methods: the printouts use
`generateEinzelwertungswettkampf(wk, wgname, …)`, the CSV and Excel exports merge with
`generateWertungsgruppenwettkampf` and split with `generateEinzelwertungswettkampf(merged, index, …)`
(`DataTableUtils:296`).

## What keeps the member age groups alike

`Regelwerk.isValid()` (`jauswertung-data/.../regelwerk/Regelwerk.java:461`) rejects a rulebook in which the member age
groups of a scoring group differ in

- the number of disciplines,
- the discipline names (per position, both genders),
- whether they have HLW.

It is called only when the rulebook editor applies its changes (`JAKsEditor.doApply`). A rulebook that arrives
another way (an old file, an imported rulebook, a competition created from a template) is not checked.

The check does not cover anything else the template supplies, among them:

- record values and lengths of the disciplines (points of the multi-event ranking),
- discipline choice, min/max members, HLW settings beyond "has HLW",
- any per-age-group setting not listed in `Wertungsgruppe.applyTo` (which sets only name, penalty-is-disqualification,
  individual ranking and individual ranking HLW).

So "first or last member" only matters for those uncovered settings. They are exactly the ones that change points, so
a different record value in one member age group gives a different ranking in the result printout than in the full
printout, protocol and exports.

## What happens to the swimmers

`ASchwimmer.setAKNummer(index, false)` keeps times, penalties and discipline choices **by position** and pads or cuts
them to the template's number of disciplines. A swimmer moved into the template is therefore ranked by the template's
discipline at the same position. While `isValid()` held, the names match; where it did not, a 100 m time can be ranked
as 50 m, or times beyond the template's discipline count are dropped silently.

Within the methods:

- `getResultWettkampf` adds a copy of the members as a new, last age group. The members stay as they are and appear
  both on their own and in the scoring group. It deep-copies the whole competition once more for every marked group
  (`Utils.copy(wk).getSchwimmer()`, line 69) only to get those copies.
- `generateWertungsgruppenwettkampf` removes the swimmers of age groups without (an existing) scoring group and moves
  the remaining ones into the first member. The other member age groups stay in the rulebook, empty. It clears
  `Wertungsgruppe` on the template, the other two do not.
- `generateEinzelwertungswettkampf(wgname)` removes every swimmer outside the named group, moves the rest into the first
  member and then builds a new competition with one age group per discipline (`generateEinzelwertungswettkampfI`).
  The merged age group itself is not returned. It neither sets nor clears the multi-event ranking flag.

Verified with four age groups (AK 0 and AK 1 in group A, AK 2 in B, AK 3 in none, one swimmer each):
`generateWertungsgruppenwettkampf` returns `A` (2 swimmers), `AK 1` (empty, still in A), `B` (1), `AK 3` (empty);
`generateEinzelwertungswettkampf(wk, "A", …)` returns two age groups `A` with one discipline and 2 swimmers each.

## Proposed first step: `generateEinzelwertungswettkampf(wgname)` reuses the merge

The CSV/Excel export already merges with `generateWertungsgruppenwettkampf` and splits with
`generateEinzelwertungswettkampf(merged, x, false)` (`DataTableUtils:280–296`). The printouts can take the same path:

```java
public static <T extends ASchwimmer> AWettkampf<T> generateEinzelwertungswettkampf(AWettkampf<T> wk, String wgname,
        boolean removeEmpty) {
    int index = indexOfFirstMember(wk.getRegelwerk(), wgname);
    if (index < 0) {
        return null;
    }
    AWettkampf<T> merged = CompetitionUtils.generateWertungsgruppenwettkampf(wk, false);
    return generateEinzelwertungswettkampf(merged, index, removeEmpty);
}
```

Why it holds:

- `generateWertungsgruppenwettkampf` does not reorder age groups, so the index of the first member in the original
  rulebook is the index of the merged age group. It is taken beforehand because the merge clears `Wertungsgruppe` on
  the template; a lookup by name afterwards could hit an ordinary age group of the same name.
- The split reads only the swimmers of the age group it is given (`ResultCalculator.getResults(wk, ak, …)`), so the
  other scoring groups in the merged competition do not matter.
- Both apply `Wertungsgruppe.applyTo` before the split, which sets the individual-ranking flag the split checks.
- The merge also sets the multi-event ranking flag; nothing in `jauswertung-data` reads it.

What changes:

- A scoring group without swimmers: today `applyTo` runs anyway; `generateWertungsgruppenwettkampf` skips it, so the
  template's own individual-ranking flag decides. The output has no swimmers either way; pin it with a test.
- An unknown group name: today a `NullPointerException` (`wg.getName()` on null); afterwards `null`, which the callers
  already handle.

Prerequisite: fix the `disableUpdates()` side effect below first, or every individual-ranking printout inherits it.

Gain: the printouts (`PrintUtils:499`, `APerDisciplinePrinter:268`, `ListOfMedalsPerDisciplinePrinter:233`) and
CSV/Excel take one path, which answers "do they produce the same results" by construction, and one of the three
copies disappears. `getResultWettkampf` remains; it differs on purpose (adds the group, keeps the members).

## Side effects

- `generateWertungsgruppenwettkampf` calls `disableUpdates()`/`enableUpdates()` on the original `wk`, not on the copy
  `wkx` it changes. On the original, `enableUpdates()` ends in `changedNow(false)`, which sets the `VERSION` and
  `LAST_CHANGE` properties. Printing or exporting a result therefore stamps the original competition as changed without
  any edit. `LAST_CHANGE` is handed to every printout as its date
  (`PrintManager.getFinalPrintable(…, wk.getLastChangedDate(), …)`, jutils-legacy), so the next printout probably shows
  the time of the previous printout instead of the last edit. Not checked yet: whether it also
  marks the file as unsaved. Meanwhile every `removeSchwimmer` on
  `wkx` runs `updateInputStatus()` once per age group, because its updates are not disabled.
- `getResultWettkampf` calls `changedNow()` on its copy only, which is harmless.

## Investigate

- Which member age group should be the template (first or last). Moot if `isValid()` is extended to record values and
  the other point-relevant settings and enforced on load as well; otherwise the choice must be made and documented.
- Whether `isValid()` should run when a competition is loaded or a rulebook is imported, and what happens to existing
  files that fail it.
- Whether the three can share one step "build the age group of a scoring group" (find members, choose the template,
  apply the settings, collect the swimmers), with the callers only deciding whether the swimmers are added as copies
  or moved, and whether the result is split per discipline.
- Whether CSV/Excel and the printouts produce the same results for a scoring group today, pinned by result tests
  before the proposed first step makes them share one path.
- Whether the `disableUpdates()` on the wrong object makes the original competition look unsaved after a printout or
  export (the stamp of `LAST_CHANGE` is certain).
- Whether swimmers should appear twice (own age group and scoring group) as in `getResultWettkampf`, or only in the
  scoring group as in the other two, and whether that is intended per output.

## Done when

The `disableUpdates()` side effect is fixed, `generateEinzelwertungswettkampf(wgname)` reuses
`generateWertungsgruppenwettkampf`, the questions above are answered and pinned by result tests, and
`getResultWettkampf` builds the age group of a scoring group with the same shared step.
