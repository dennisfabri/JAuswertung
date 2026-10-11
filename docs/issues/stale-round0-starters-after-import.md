# Round 1 can use stale starters after an import

## Finding

For team competitions with finals, the starters of round 0 exist twice: in the per-discipline array
(`ASchwimmer.setStarter(int, …)`) and in the round's `Eingabe` (`ASchwimmer.setStarter(String id, …)`).

- The starter import (`ImportManager`, starters import) writes both.
- The UI for round 0 (`JStarterPerDiscipline`) and `CompetitionUtils.getStarter` only use the array.
- `HeatsUtils`, when creating round 1, takes the starters of the previous round from the `Eingabe` first and only
  falls back to the array if there is none.

If round-0 starters are changed in the UI after an import, round 1 is built from the outdated imported value.

## Done when

- Round 0 starters live in one place only (the array); the import no longer writes the round-0 `Eingabe`, and
  `HeatsUtils` reads round 0 through `CompetitionUtils.getStarter`.
- A test covers: import starters, change them for round 0, create round 1, round 1 uses the changed starters.
