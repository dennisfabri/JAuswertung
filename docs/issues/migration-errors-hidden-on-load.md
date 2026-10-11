# Migration errors are hidden when loading a competition

## Finding

`IOUtils.fromXML(Reader)` (`jauswertung-io/.../io/IOUtils.java`) loads a file with
`VersionedDocument.fromXML(xml).toBean()`, which runs the `migrateN` steps. If that throws a `RuntimeException`, the
exception is only logged at debug level and the file is read again with plain `xstream.fromXML(xml)`, without any
migration. That second attempt fails on the first renamed or removed field and its exception is what the user sees.

The reported error therefore points at a field the migration would have handled, not at the step that actually failed.

## Reproduction

Before the fix of `Laufliste.migrator3` for `blocks`, loading `compatibility/2021/Mannschaft.wk` reported a missing
field `Einteilung.first`, although the failing element was `LauflisteBlockEinteilung.first` inside `blocks`.

## Done when

A failing migration is reported with its own exception (at least logged at warn/error level, or rethrown with the
plain-XStream exception as suppressed), and the plain-XStream fallback is either removed or only used for files that
carry no version.
