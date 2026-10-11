# Open Issues

Open findings from code work that are not fixed yet. Each issue is a self-contained file; once it is fixed and
verified, delete the file and its row here (see `AGENTS.md` → *docs/issues/*).

| Issue | Summary |
| --- | --- |
| [console-output-and-printstacktrace.md](console-output-and-printstacktrace.md) | 166 `printStackTrace()` and 169 `System.out`/`System.err` calls bypass logging and error handling |
| [custom-heat-split-with-mixed-startgroups.md](custom-heat-split-with-mixed-startgroups.md) | Custom heat split throws in `reorderDisciplines` when a start group mixes discipline counts |
| [migration-errors-hidden-on-load.md](migration-errors-hidden-on-load.md) | A failing file migration is swallowed and reported as an unrelated XStream error |
| [best-times-for-heat-based-competitions.md](best-times-for-heat-based-competitions.md) | Pool competitions with finals have times per round but no best times |
| [disqualified-place-in-finals.md](disqualified-place-in-finals.md) | Disqualified swimmers get a place in finals; they should only keep their points |
| [stale-round0-starters-after-import.md](stale-round0-starters-after-import.md) | Round 1 can be built from outdated round-0 starters after an import |
| [timelimit-check-always-disabled.md](timelimit-check-always-disabled.md) | The time limit check can never be started |
| [teammember-constructor-ignores-import-id.md](teammember-constructor-ignores-import-id.md) | A `Mannschaftsmitglied` constructor drops its import id |
| [round-competition-removes-wrong-discipline.md](round-competition-removes-wrong-discipline.md) | `createCompetitionFor` removes the selected round instead of the others |
