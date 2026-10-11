# Saved block split is never restored

`Laufliste.blocks` stores the block split chosen in the heat wizard, but it never takes effect when read back:
`BlockEinteilung` has no `equals()`, so `Laufliste.getBlocks()` compares the saved entries with freshly built standard
blocks by identity, finds none, sets every saved entry to `null` and returns the standard order. The wizard
(`JNewHeatsWizard`) therefore always starts with the standard order.

`blocks`, `mode` and `verteilung` (`verteilung2` in files) are only the wizard's last settings; the heats themselves do
not depend on them.

## Decide

- Fix: add `equals`/`hashCode` to `BlockEinteilung` (and a test that the saved order is restored), or
- Remove: drop `blocks` (with `omitField` for old files), the `blocks` part of `Laufliste.migrator3`, and possibly
  `mode`/`verteilung` as well.
