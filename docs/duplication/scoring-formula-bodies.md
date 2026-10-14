# Scoring formula bodies are copied

In `jauswertung-data/.../util/ergebnis/`:

- `setPoints`/`toResults` (about 270 lines) are copied in `FormelDLRG2007` and `FormelDirectPoints`.
- The "severe penalty first" comparator exists in `FormelDLRG2007:33-54`, `FormelFeuerwehr2005:62-83`,
  `FormelILS:27-57` and `FormelILSSERC:32-52`.

A third copy in `FormelLinear` had already drifted; it now extends `FormelDLRG2007` and only overrides `getResult`.
`FormelILSSERC` ranks differently by design and stays separate.

## Done when

An abstract `ATimeBasedFormel` holds ranking, judges' decision and `toResults` with `getResult(time, rec, …)` as the
hook, and a shared `SeverePenaltyComparator` replaces the copies; the `test/results/dm*` tests and new
DirectPoints tests pass.
