# Replace swingbits

`org.oxbow:swingbits` (task dialogs, table filter, menu builder) is a fork of a dead library, maintained by hand in
`swingbits/`. `jutils-legacy` and JAuswertung depend on it.

Check which parts are used. Options: inline them into `jutils-legacy`, or replace the table filter and dialogs with
plain Swing/FlatLaf.

## Done when

No project depends on `swingbits`; `build-dependencies.sh` and the workspace `AGENTS.md` no longer list it.
