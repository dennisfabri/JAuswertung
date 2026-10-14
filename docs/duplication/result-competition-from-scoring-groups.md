# Result competition from scoring groups is built twice

`jauswertung-plugins-editor/.../core/ResultSelectionUtils.java:55-97` (`getResultWettkampf`) and
`jauswertung-plugins-print/.../ResultsPrinter.java:353-393` (`getWettkampf`) are identical (38 lines, 315 tokens);
they have not drifted yet. plugins-print already depends on plugins-editor.

Related: `CompetitionUtils.generateWertungsgruppenwettkampf` (`CompetitionUtils.java:465-520`) and
`ResultUtils.generateEinzelwertungswettkampf` (`ResultUtils.java:327-357`) also copy the scoring group flags into an
age group, plus `Einzelwertung`/`EinzelwertungHlw`/`Mehrkampfwertung`.

## Done when

`ResultsPrinter` calls `ResultSelectionUtils.getResultWettkampf(core.getFilteredWettkampf())`, and the flag copying
lives in one place (e.g. `Wertungsgruppe.applyTo(Altersklasse)`).
