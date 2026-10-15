# Code Duplication

Duplicated code found by a review of `main` at 403b3715 (PMD CPD 7.7.0, `--minimum-tokens 100`, all `src/main/java`
folders of this repository and of jutils-legacy in one run, plus manual review for duplicated rules CPD cannot see).
Each finding is a self-contained file listed in the table below; once it is resolved, delete its file and its row here
(same rule as `docs/issues/`). Keep this README with its table even when no findings are open.

Every row has a severity and a size; keep the table ordered by severity (High, Medium, Low), then by size (S, M, L).

- **Severity:** High = the copies have drifted into wrong results; Medium = the copies have drifted into a feature
  that fails or misleads, or duplicated logic invites such drift; Low = only redundancy, or drift without
  user-visible effect.
- **Size:** S = a local change of a few lines with a test; M = several classes or a migration; L = spread over many
  modules or needs a design decision.

Findings that need a decision before they can be resolved have a *Decide* section instead of *Done when*.

| Finding | Severity | Size | Summary |
| --- | --- | --- | --- |
| [Heat chunking in TableHeatUtils is copied three times](table-heat-chunking.md) | Low | S | Splitting into heats and merging sparse tables is repeated in `TableHeatUtils` |
| [Sex labels are looked up by hand with different fallbacks](sex-labels-by-hand.md) | Low | S | Several places bypass the `I18n` sex helpers; `TimeLimitsPlugin` falls back to English |
| [RecordsUpdater's copy of the time parser recurses forever](records-updater-time-parser.md) | Low | S | Developer tool: text times end in `StackOverflowError` |
| [PrinterCollection exists twice](printer-collection-twice.md) | Low | S | Two `PrinterCollection` classes with different `Printer` interfaces |
| [Referee table creators are copied](referees-table-creators.md) | Low | S | `RefereesTableCreator` and `RefereesTableCompactCreator` share code and hard-code "Stufe" |
| [JAuswertung re-implements jutils helpers](reimplemented-jutils-helpers.md) | Low | S | `Check.format` and `BugReport.asReadableUnit` duplicate `StringTools` |
| [Excel2007Utils and ExcelReader are parallel facades](excel-reader-facades.md) | Low | S | Same facade for XSSF and HSSF |
| [RegisteredTeamnamesPrinter2 copies RegisteredTeamnamesPrinter](jteams-teamnames-printer.md) | Low | S | jteams copies a printer it could use from plugins-print |
| [Small helpers are copied between plugins](small-plugin-helpers.md) | Low | S | `addInfoToGlass`, arrow-key navigation and `createCompetitionFor` wrappers |
| [Editor dialogs copy their frame](editor-dialog-frame.md) | Low | M | Buttons, ENTER/ESC bindings and strategy are copied across five editor dialogs |
| [AresWriterBlocks copies the ARES writer](ares-writer-blocks.md) | Low | M | Developer tool in misc copies and has drifted from `jauswertung-ares` |
| [Test code is heavily duplicated](test-code-duplication.md) | Low | L | 235 near-identical championship test classes and copied ILS finals fixtures |

## Reviewed and kept

These look alike but were left as they are:

- `Laufliste.startgruppeVerteilenILS`/`…ILSOutdoor`: only the setup is shared; the distribution algorithms differ on
  purpose.
- `Team.FromCode00`/`FromCode01`: versioned readers for the file format; separate for compatibility.
- `FormelILSSERC` comparator: different ranking rules by design.
- `DataTableUtils` time as text vs. as an Excel day fraction (`:1705`/`:1770`): different on purpose.
- `CsvExporter`/`ASpreadsheetExporter`: thin wrappers that already delegate to `DataTableUtils`.
- Long/short variants (`Mannschaft.getStarter`/`getStarterShort`, `Lauf.getDisziplin`/`getDisziplinShort`,
  `ZWUtils.getHLWStatus`/`…Kompakt`, `CompetitionUtils.getOrganizationAmounts`/`getQOrganizationAmounts`), and small UI
  boilerplate (wizard pages, drop target listeners, `JNeuerWettkampf`/`JNeueKV`, akeditor group panels).
