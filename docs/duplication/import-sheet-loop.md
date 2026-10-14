# The sheet loop of the table imports is copied six times

`jauswertung-io/.../ImportUtils.java` repeats the per-sheet loop — feedback, empty-sheet check, header extraction,
`identifyIndizes`, missing-header message (CPD 202–246 tokens, about 30 lines each) — in `tablesToTeammembers`
(`:62`), `tablesToStarters` (`:184`), `tablesToZWResult` (`:1175`), `tablesToRegistration` (`:1290`),
`tablesToRegistrationUpdate` (`:1360`) and `tablesToReferees` (`:~1440`).

Teammembers and starters pass `null` as the file name (`:91`, `:214`), so their header errors do not name the file;
the other imports pass `file`. The missing-header messages are built in two styles.

Related copies:

- `CsvImporter.registration`/`registrationUpdate` (`CsvImporter.java:76-120`, `187-231`, 299 tokens) copy
  `CsvUtils.read` (`CsvUtils.java:37-65`) except for `maxline++`.
- `EasyWKImporter.java:41-50,77` copies `SEPARATORS` and `identifySeparator`.

## Done when

A `forEachSheet(fb, sheets, tables, file, required, SheetHandler)` template in `ImportUtils` and a
`CsvUtils.read(is, extraColumns)` replace the copies, and every import reports the file name.
