# JAuswertung re-implements jutils helpers

- `jauswertung-plugins-checks/.../emergency/Check.java:112-117` `format(int)` is `StringTools.zeitString(zeit, '.')`.
- `BugReport.java:85-99` `asReadableUnit` overlaps `StringTools.sizeToString` (different output format; low value).

## Done when

`Check` uses `StringTools.zeitString`; `asReadableUnit` is replaced if the output format of `sizeToString` is
acceptable.
