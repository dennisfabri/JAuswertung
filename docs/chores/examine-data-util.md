# util in jauswertung-data is not classified

`de.df.jauswertung.util` in `jauswertung-data` holds 53 classes in `util` itself and in `data`, `ergebnis` (scoring
formulas), `format`, `valueobjects` and `vergleicher` (comparators). *Packages in jauswertung-data* (`AGENTS.md`)
leaves it unclassified until it has been examined; the model already depends on it heavily (see
`daten-outgoing-references.md`).

Known findings so far: `CompetitionUtils` builds a Swing `TableModel` and `Utils` holds the UI performance mode
(`data-without-swing-and-print.md`), `Team` holds the team-code format (`team-code.md`), `RandomUtils` only returns
`new Random()`.

## Done when

Every class in `util` is classified — model logic (scoring, ranking, comparisons), a data class, a general utility, or
something that belongs in another module — the result is written down as a rule in *Packages in jauswertung-data*,
and the misplaced classes have their own chores.
