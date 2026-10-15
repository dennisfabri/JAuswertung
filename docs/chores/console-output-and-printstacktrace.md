# Replace printStackTrace() and System.out/System.err

## Finding

- `printStackTrace()`: 166 calls in 81 files across almost all modules (most in `jauswertung-io` with 16 files,
  `jauswertung-plugins-print` 7, `jauswertung`, `-data`, `-print`, `-plugins-input` with 6 each).
- `System.out` / `System.err`: 169 calls in 44 files. `jauswertung-misc` has 17 files; there these are command-line
  developer tools and console output may be intended.

`DefaultInit` redirects `System.out`/`System.err` to SLF4J (`SystemStreamsToLog`), so the output does reach the log, but
without logger name, without a meaningful level, and with stack traces split into one log event per line.

A `printStackTrace()` in a `catch` block usually also means the failure is swallowed: the user gets no feedback that
the action failed, and the code continues as if it succeeded.

## Suggested approach

- Per occurrence, decide what the catch block should do (see `AGENTS.md` → *Exceptions* and *Logging*): handle and
  report the failure to the user, propagate it, or log it with `@Slf4j` at the right level. Do not just swap
  `e.printStackTrace()` for `log.error("", e)` where the failure should reach the user.
- Check whether a log message would include participant data or credentials before adding it.
- Leave intended console output of `main`-method tools in `jauswertung-misc` (and `AresCmd`, `DPSplitter`) alone, or
  move it to a logger only when it is diagnostic output.
- Work module by module, one commit each.

## Done when

No `printStackTrace()` remains, and `System.out`/`System.err` are used only for intended console output of
command-line tools.
