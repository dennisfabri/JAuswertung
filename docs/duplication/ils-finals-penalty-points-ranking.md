# ILS finals rank penalty points the old way

`FormelILSFinals.java:86-136` (`jauswertung-data/.../util/ergebnis/`) is a copy of the comparator in
`FormelILS.java:27-83`. Commit a5442df0 ("Formel für Fehlerpunkte erweitert") changed only `FormelILS`: a swimmer with
penalty points is no longer zeroed (`// zeroed1 = true;` at `FormelILS.java:61`) but ranked by time, and the points
are deducted in `getPoints` (`FormelILS.java:400-408`).

The copy still sets `zeroed1 = true` (`FormelILSFinals.java:116`). Finals rank through `ComparatorT2` →
`ILSComparator` (`:214`, `:80`) and also deduct the points in `getPoints` (`:244`). A swimmer with penalty points is
therefore placed behind every unpenalised swimmer in finals *and* loses the points — punished twice — while
preliminaries apply the new rule. No test covers penalty points in finals; the `indoorfinals` tests only use
`DISQUALIFIKATION`.

## Done when

A finals test with penalty points exists and `FormelILSFinals` uses the comparator of `FormelILS` (made
package-visible) instead of its own copy.
