# Print previews read Swing widgets off the EDT

Commit 98261395 made every print action read its widget state first (e.g. `ResultsPrinter.java:289-294`). The preview
path is duplicated in each printer and still calls a no-argument `getPrintable()` that reads widgets; jutils
`JPrintPreview` runs it inside a `SwingWorker` (`JPrintPreview.java:291-301`). This breaks the AGENTS.md rule "never
read Swing components inside the lambda". Checked examples:

- `ResultsPrinter.java:236-238`, `:301-310`
- `ResultsWithDetailedFilterPrinter.java:190-193`, `:237-254`
- `ListOfMedalsPrinter.java:131-133`, `ListOfMedalsPerDisciplinePrinter.java:237-239`
- `BrokenLimitsPrinter.java:180-182`, `BrokenRecordsPrinter.java:204-206`
- `ALauflistenPrinter` (`comments.isSelected()` in `create()`), `SprecherlistePrinter` (`columns.getSelectedIndex()`)

The root cause is boilerplate copied into about 25 printers (`PrintActionListener`, `PreviewActionListener`,
`PPrintableCreator`). `BestzeitenPrinter`, `DisciplinesPrinter`, `InfopagePrinter`, `RefereePrinter` and
`RulebookPrinter` share an identical 37-line block (`:37-75`); further pairs: `BrokenLimits`/`BrokenRecords`,
`RegisteredTimes`/`Weitermeldung`, `Laufergebnisse`/`Lauflisten`, `Fehlermeldekarten`/`Zieleinlaufkarten`,
`Documents`/`Protocol`.

## Done when

A base printer reads the widget state once on the EDT (`captureState()`) and builds the printable from it
(`create(state)`) for both print and preview; the printers no longer carry their own listeners.
