# Replace l2fprod-common

`org.lisasp.legacy:l2fprod-common-*` is a fork of the dead L2FProd Swing components, maintained by hand in
`l2fprod-common/`.

Replaced by own components in `jutils-swing` (`de.df.jutils.gui`) on 2026-10-15: `JButtonBar`, `TipOfTheDay` with
`TipOfTheDayPanel`/`TipOfTheDayDialog`, and `JStatusBar` (used by `JPFrame`). `DesignInit` sets the table, tooltip and text field fonts itself (`tweakFonts()`), and the discipline list of `JTimesInputPanel` uses the JDK's `DefaultListCellRenderer`. `JFontChooser` is replaced by the one in `jutils-fontchooser` (`de.df.jutils.gui.fontchooser`, German/English); unlike the old one it always has a selection, so `JVeranstaltungsschriftart` returns `null` until the user chose a font.

## Still used (checked 2026-10-15)

| Component | Used in | Replacement |
| --- | --- | --- |
| `JTaskPaneGroup` | 9 files (registration plugins, `JSchwimmerEditieren`, `JSimpleInputPanel`, `JTeammembersInputPanel`, `PPrintPlugin`, ...) | own collapsible group |
| `BasicTaskPaneGroupUI` | `GradientTaskPaneGroupUI` (`jutils-legacy`) | goes away with the task pane |
## Done when

No project depends on `l2fprod-common`; `build-dependencies.sh` and the workspace `AGENTS.md` no longer list it.
