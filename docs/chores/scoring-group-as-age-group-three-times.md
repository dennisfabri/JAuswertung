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

Gain: the printouts (`PrintUtils:499`, `APerDisciplinePrinter:268`, `ListOfMedalsPerDisciplinePrinter:233`) and
CSV/Excel take one path, which answers "do they produce the same results" by construction, and one of the three
copies disappears. `getResultWettkampf` remains; it differs on purpose (adds the group, keeps the members).

## Decided: settings of the merged age group

The merged age group no longer copies a template; each setting follows one rule. `Altersklasse` / `Disziplin` field
names in brackets.

| Setting | Rule |
| --- | --- |
| Name, penalty is disqualification, individual ranking, individual ranking HLW | from the scoring group (`Wertungsgruppe.applyTo`) |
| Multi-event ranking (`keineMehrkampfwertung`) | from the scoring group (`isProtokollMitMehrkampfwertung`) |
| Number of disciplines, discipline names, HLW (`hlw`) | must match (already in `isValid()`) |
| Discipline choice (`diszchoice`) | must match; `mindisz`, `useddisz`, `maxdisz` must match only when it is switched on |
| Team size (`minMembers`, `maxMembers`) | must match |
| Record value per discipline and gender (`rec`) | smallest non-zero value; 0 if all are 0 |
| Age limits (`alter`, `alterMannschaft`) | lowest lower and highest upper limit; 0 ("no limit") if any member has 0 for that bound |
| Overall ranking per gender (`gesamtwertung`) | enabled if enabled in at least one member |
| Start group (`startgruppe`) | does not matter; cleared |
| Discipline length and laps (`laenge`, `laps`) | do not matter (timekeeping only) |
| Rounds and lane rotation (`runden`, `rundenIds`, `laufrotation`) | do not matter (heat-based competitions are excluded by `isValid()`) |

`Regelwerk.isValid()` is extended by the new "must match" rules. It keeps running only when the rulebook editor
applies its changes: neither a new competition (rulebook file from `JNeuerWettkampf`) nor opening a competition file
checks it.

A file is loaded unchecked, so its member age groups may break a "must match" rule. Then the merged age group takes
that setting from the first member; `getResultWettkampf` switches from the last member to the first.

Swimmers appearing twice in `getResultWettkampf` (own age group and scoring group) is intended.

## Investigate

- Whether CSV/Excel and the printouts produce the same results for a scoring group today, pinned by result tests
  before the proposed first step makes them share one path.

## Done when

`Regelwerk.isValid()` checks the new "must match" rules, the three methods build the merged age group with one shared
step that follows the rules above (pinned by result tests), `generateEinzelwertungswettkampf(wgname)` reuses
`generateWertungsgruppenwettkampf`, and `getResultWettkampf` uses the same shared step.
