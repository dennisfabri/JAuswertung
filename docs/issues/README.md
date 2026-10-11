# Open Issues

Open findings from code work that are not fixed yet. Each issue is a self-contained file; once it is fixed and
verified, delete the file and its row here (see `AGENTS.md` → *docs/issues/*).

| Issue | Summary |
| --- | --- |
| [console-output-and-printstacktrace.md](console-output-and-printstacktrace.md) | 166 `printStackTrace()` and 169 `System.out`/`System.err` calls bypass logging and error handling |
| [custom-heat-split-with-mixed-startgroups.md](custom-heat-split-with-mixed-startgroups.md) | Custom heat split throws in `reorderDisciplines` when a start group mixes discipline counts |
| [migration-errors-hidden-on-load.md](migration-errors-hidden-on-load.md) | A failing file migration is swallowed and reported as an unrelated XStream error |
