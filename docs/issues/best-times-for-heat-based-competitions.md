# Best times are missing for heat-based competitions with times

## Finding

Competitions with finals and times (formulas `FormelILSFinals` and `FormelDLRG2007Finals`, i.e. `isHeatBased()` with
`DataType.TIME`) have no best times: `AWettkampf.hasBestTimes()` disables the export and the printer
(`BestzeitenPrinter`) for every heat-based competition.

The reason is the data source, not the competition: `DataTableUtils.getSchnellsteZeiten` reads the time of the single
round (`ASchwimmer.getZeit(int)`). Heat-based competitions store their times per round in `ASchwimmer.eingaben`, keyed
by the `OWDisziplin` id (`ak-m/f-discipline-round`, read via `getZeit(String id)`); the plain times stay empty, so the
table listed every swimmer with `0:00,00` before the export was disabled.

Competitions that record places instead of times (`DataType.RANK`, open water) have no times at all and stay excluded.

## Done when

- Heat-based competitions with times export and print best times, taken from all rounds of a discipline (the fastest
  valid time of each swimmer across heats, semifinals and final).
- Times with a disqualifying penalty are ignored, as for competitions without finals.
- `AWettkampf.hasBestTimes()` only excludes `DataType.RANK`.
- A compatibility fixture with a heat-based pool competition (e.g. rulebook with `ILS2004Finals`) covers the export.
