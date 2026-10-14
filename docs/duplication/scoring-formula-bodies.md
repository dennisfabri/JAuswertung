# Scoring formula bodies are copied

In `jauswertung-data/.../util/ergebnis/`:

- `setPoints`/`toResults` (about 270 lines) are copied in `FormelDLRG2007`, `FormelDirectPoints` and `FormelLinear`;
  `DLRGComparator`/`RankComparator(2)` in DLRG2007 (`:28-79`) and Linear.
- The finals ranking / judges' decision (Zielrichterentscheid) block also appears in `FormelMedaillen.java:219-255`.
- The "severe penalty first" comparator exists in `FormelDLRG2007:33-54`, `FormelFeuerwehr2005:62-83`,
  `FormelILS:34-54`, `FormelILSSERC:32-52`, `FormelILSFinals:89-109` and `FormelLinear:30-51`.

The copies have already drifted, see [ILS finals rank penalty points the old way](ils-finals-penalty-points-ranking.md)
and [FormelLinear has drifted from FormelDLRG2007](formel-linear-drifted.md). `FormelILSSERC` and `FormelMedaillen`
rank differently by design and stay separate.

## Done when

An abstract `ATimeBasedFormel` holds ranking, judges' decision and `toResults` with `getResult(time, rec, …)` as the
hook, and a shared `SeverePenaltyComparator` replaces the copies; the `test/results/dm*` tests and new
Linear/DirectPoints tests pass.
