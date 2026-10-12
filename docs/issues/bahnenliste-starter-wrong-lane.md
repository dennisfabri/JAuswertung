# Bahnenliste shows starters of the wrong discipline for multi-line teams

## Finding

`BahnenListe.toBahnenListe(int puppe)` (`jauswertung-plugins-print/.../gui/util/BahnenListe.java`) builds one row per
heat for lane `puppe`. For a team in a multi-line competition whose name equals its organisation, the name column is
taken from `m.getStarterShort(lauf.getDisznummer(x), ", ")`. `x` is the index of the heat, not the lane; the lane is
`puppe - 1`, which the discipline column below already uses.

## Effect

The lane list prints the starters of the discipline that the heat holds in lane `x`: the wrong starters when that lane
has another discipline, nothing useful when it is empty, and an `IndexOutOfBoundsException` (caught and printed) when
there are more heats than lanes.

## Suggested fix

Use `lauf.getDisznummer(puppe - 1)` and cover it with a test on a team competition with mixed disciplines in one heat.
