# Replace System.out/System.err with logging

## Finding

`System.out`/`System.err` appear in 118 lines in 34 files (`src/main`, commented-out lines included; counted with
`grep`). By module: `jauswertung-misc` 45, `jauswertung` 22, `jauswertung-data` 13, `jauswertung-ares` 12,
`jauswertung-plugins-checks` 8, `jauswertung-io` 5, `jauswertung-veranstaltungswertung` 4, `jauswertung-plugins-httpserver`
3, `jauswertung-plugins-print` and `-input` 2 each, `jauswertung-swing` and `-plugins-core` 1 each.

`DefaultInit` redirects both streams to SLF4J (`SystemStreamsToLog`), so the output reaches the log, but under the
logger names `System.out`/`System.err`, always at INFO or ERROR, and without the class that produced it.

## Suggested approach

- Replace each call with `@Slf4j` logging at the right level (`debug` for diagnostics, `info`, `warn`, `error`) and
  parameterized messages. Remove debug output that is commented out or no longer useful instead of converting it.
- Check whether a message would include participant data or credentials before logging it.
- Leave output that is the purpose of a command-line tool (`main` methods in `jauswertung-misc`, `AresCmd`,
  `DPSplitter`) alone: its result is the console output.
- Work module by module, one commit each.

`printStackTrace()` is tracked separately in [replace-printstacktrace.md](replace-printstacktrace.md).

## Done when

`System.out`/`System.err` are used only for intended console output of command-line tools, and the logger names
`System.out`/`System.err` no longer show up in a normal log.
