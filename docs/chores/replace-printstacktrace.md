# Replace printStackTrace()

## Finding

- `printStackTrace()`: 166 calls in 81 files across almost all modules (most in `jauswertung-io` with 16 files,
  `jauswertung-plugins-print` 7, `jauswertung`, `-data`, `-print`, `-plugins-input` with 6 each).

`System.out`/`System.err` output is tracked separately in
[replace-system-out-with-logging.md](replace-system-out-with-logging.md). `printStackTrace()` writes to `System.err`, which
`DefaultInit` redirects to SLF4J (`SystemStreamsToLog`), so the output does reach the log, but without logger name or
meaningful level, and with the stack trace split into one log event per line.

A `printStackTrace()` in a `catch` block usually also means the failure is swallowed: the user gets no feedback that
the action failed, and the code continues as if it succeeded.

## Suggested approach

- Per occurrence, decide what the catch block should do (see `AGENTS.md` → *Exceptions* and *Logging*): handle and
  report the failure to the user, propagate it, or log it with `@Slf4j` at the right level. Do not just swap
  `e.printStackTrace()` for `log.error("", e)` where the failure should reach the user.
- Check whether a log message would include participant data or credentials before adding it.
- Work module by module, one commit each.

## Done when

No `printStackTrace()` remains.
