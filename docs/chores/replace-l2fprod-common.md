# Replace l2fprod-common

`org.lisasp.legacy:l2fprod-common-*` is a fork of the dead L2FProd Swing components, maintained by hand in
`l2fprod-common/`.

Replaced by own components in `jutils-swing` (`de.df.jutils.gui`) on 2026-10-15: `JButtonBar`, `TipOfTheDay` with
`TipOfTheDayPanel`/`TipOfTheDayDialog`, and `JStatusBar` (used by `JPFrame`). `DesignInit` sets the table, tooltip and text field fonts itself (`tweakFonts()`), and the discipline list of `JTimesInputPanel` uses the JDK's `DefaultListCellRenderer`.

## Still used (checked 2026-10-15)

| Component | Used in | Replacement |
| --- | --- | --- |
| `JTaskPaneGroup` | 9 files (registration plugins, `JSchwimmerEditieren`, `JSimpleInputPanel`, `JTeammembersInputPanel`, `PPrintPlugin`, ...) | own collapsible group |
| `BasicTaskPaneGroupUI` | `GradientTaskPaneGroupUI` (`jutils-legacy`) | goes away with the task pane |
| `JFontChooser` | `JFontDialog`, `MOptionenPlugin`, `OPrintOptionsPlugin`, `JVeranstaltungsschriftart` | own dialog (family, size, bold, italic) |

## Font chooser: evaluated, not adopted (2026-10-15)

`org.drjekyll:fontchooser` 3.1.0 (LGPL-3.0, Java 11+, no dependencies, last release July 2024) is a drop-in for the three
call sites, but it is only partly translated: the style names ("plain", "bold", ...) come from the font name and the
"Monospaced only" checkbox is hardcoded in English. It would need a small wrapper that translates both through `I18n`.
Its chooser selects a font immediately, while the old one selects none, so `JVeranstaltungsschriftart` has to keep
returning `null` until the user chooses. Its packed size is tiny; `JFontDialog` needs an explicit preferred size.

## Done when

No project depends on `l2fprod-common`; `build-dependencies.sh` and the workspace `AGENTS.md` no longer list it.
