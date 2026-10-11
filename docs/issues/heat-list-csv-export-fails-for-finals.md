# CSV heat list export fails for competitions with finals

## Finding

`ExportManager.isEnabled` enables `HEAT_LIST` when the heat list for finals (`getLauflisteOW()`) is not empty, but
`DataTableUtils.heats` calls `Laufliste.hasMixedHeats()`, which streams over the heat list for competitions without
finals. That list is `null` in competitions with finals, so the export throws a `NullPointerException`. The exception
is only printed and the export returns `false`.

Visible in `CompatibilityTest`: the finals fixtures have no `HEAT_LIST.csv`, and the test log contains
`Cannot invoke "java.util.LinkedList.stream()" because "this.laufliste" is null` from `Laufliste.hasMixedHeats`.

## Done when

The heat list of a competition with finals can be exported as CSV (or the export is not offered), and the
compatibility snapshots for the finals fixtures contain it.
