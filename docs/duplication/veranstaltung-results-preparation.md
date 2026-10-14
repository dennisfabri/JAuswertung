# Event-scoring results prepare their input twice

`jauswertung-veranstaltungswertung/.../ResultsExporter.java:100-135` and `ResultsPrinter.java:87-123` copy the
filtering by qualification level and the collection of sexes and competition sizes (CPD 325 tokens).

The exporter's private `buildGesamtwertungsergebnis` (`:150-156`) skips the `gesamt.doesNotHaveAnyPoints()` check of
`TableUtils.buildGesamtwertungsergebnis` (`jauswertung-print/.../TableUtils.java:65-79`).

## Done when

The preparation lives in `VeranstaltungsUtils` and export and printout use the same result builder.
