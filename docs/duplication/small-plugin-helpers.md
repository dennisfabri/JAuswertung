# Small helpers are copied between plugins

- `PTeammembersScanPlugin.java:48-67` and `PTeammembersInputPlugin.java:56-75` both contain `addInfoToGlass`.
- The arrow-key navigation listeners are copied between `PTimeInputPlugin.java:991-1028` and
  `JSimpleInputPanel.java:616-651`.
- `createCompetitionFor(OWSelection)` is the same three-line wrapper in `AFilledKartenPrinter:149`,
  `LauflistenPrinter:125`, `ALauflistenPrinter:123`, `LaufergebnissePrinter:122` and `JOWHeatsPlugin:317`.

## Done when

Each helper exists once.
