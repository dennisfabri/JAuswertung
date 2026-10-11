# Open Issues

Open findings from code work that are not fixed yet. Each issue is a self-contained file; once it is fixed and
verified, delete the file and its row here (see `AGENTS.md` → *docs/issues/*).

Every row has a severity and a size; keep the table ordered by severity (High, Medium, Low), then by size (S, M, L).

- **Severity:** High = wrong results or lost/corrupted data; Medium = a feature fails or misleads, with a workaround;
  Low = no user-visible effect, or only inconvenience.
- **Size:** S = a local change of a few lines with a test; M = several classes or a migration; L = spread over many
  modules or needs a design decision.

| Issue | Severity | Size | Summary |
| --- | --- | --- | --- |
| [custom-heat-split-with-mixed-startgroups.md](custom-heat-split-with-mixed-startgroups.md) | Medium | M | Custom heat split throws in `reorderDisciplines` when a start group mixes discipline counts |
| [heat-list-csv-export-fails-for-finals.md](heat-list-csv-export-fails-for-finals.md) | Medium | M | CSV heat list export throws for competitions with finals |
| [round-competition-removes-wrong-discipline.md](round-competition-removes-wrong-discipline.md) | Low | S | `createCompetitionFor` removes the selected round instead of the others |
| [teammember-constructor-ignores-import-id.md](teammember-constructor-ignores-import-id.md) | Low | S | A `Mannschaftsmitglied` constructor drops its import id |
| [best-times-for-heat-based-competitions.md](best-times-for-heat-based-competitions.md) | Low | M | Pool competitions with finals have times per round but no best times |
| [console-output-and-printstacktrace.md](console-output-and-printstacktrace.md) | Low | L | 166 `printStackTrace()` and 169 `System.out`/`System.err` calls bypass logging and error handling |
