# Export button of the per-discipline printers is always disabled

`APerDisciplinePrinter` (`jauswertung-plugins-print/.../gui/plugins/print/`) shows an Export button (`:86-88`, `:116`)
that is enabled only when `hasExport()` returns true (`:177`). Neither subclass does: `ResultsPerDisciplinePrinter`
changed it from `true` to `false` in 8e8d7921, and `DocumentsEinzelwertungPrinter` inherits the default `false`. So the
panels "Ergebnisse je Disziplin" and "Urkunden Einzelwertung" show an Export button that can never be clicked.

The export itself only handles heat-based competitions: `ExportActionListener` (`:434`) asks for a round and writes it
with `export(OWSelection)`; for other competitions it does nothing, which is probably why 8e8d7921 disabled the button.
As a side effect, the export of a round to Excel is no longer reachable for heat-based competitions either. The empty
hooks `export(AWettkampf, OutputStream)` in both classes are leftovers.

## Done when

Either the button is enabled where the export works (e.g. `ResultsPerDisciplinePrinter.hasExport()` returns
`wk.isHeatBased()`), or the button, `ExportActionListener` and the export code are removed.
