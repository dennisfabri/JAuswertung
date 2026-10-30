# References from daten to other packages are unreviewed

*Packages in jauswertung-data* (`AGENTS.md`) requires every reference from `de.df.jauswertung.daten` to another
package to be examined individually. Not listed, because they are allowed: `java.util`, `java.lang`, Lombok, and the
persistence mapping (XStream annotations, commons.xmt, dom4j — see *Dependency Rules*).

Paths are relative to `jauswertung-data/.../daten/`. Mark each row with a verdict (keep, with the reason — or a fix,
moved to its own chore or done here) and remove it once settled.

| From | To | Note |
| --- | --- | --- |
| `ASchwimmer` | `util.SearchUtils` | |
| `ASchwimmer` | `jutils.util.StringTools` | |
| `AWettkampf` | `gui.util.I18n` | |
| `AWettkampf` | `util.SchwimmerUtils` | |
| `AWettkampf` | `util.ergebnis.DataType` | |
| `AWettkampf` | `util.ergebnis.FormelManager` | |
| `AWettkampf` | `util.SearchUtils` | |
| `AWettkampf` | `util.Utils` | |
| `EinzelWettkampf` | `util.AltersklassenUtils` | |
| `MannschaftWettkampf` | `util.AltersklassenUtils` | |
| `MannschaftWettkampf` | `util.RandomUtils` | returns `new Random()`; could be inlined |
| `Teilnehmer` | `util.SchwimmerUtils` | |
| `Zielrichterentscheid` | `util.SearchUtils` | |
| `laufliste/Lauf` | `gui.util.I18n` | |
| `laufliste/Lauf` | `jutils.util.StringTools` | |
| `laufliste/Laufliste` | `util.BetaFeature` | feature flag inside the model |
| `laufliste/Laufliste` | `util.CompetitionUtils` | |
| `laufliste/Laufliste` | `util.RandomUtils` | returns `new Random()`; could be inlined |
| `laufliste/Laufliste` | `util.SearchUtils` | |
| `laufliste/OWLauf` | `util.SearchUtils` | |
| `laufliste/OWLauf` | `jutils.util.StringTools` | |
| `regelwerk/Altersklasse` | `jutils.util.StringTools` | |
| `regelwerk/Regelwerk` | `util.ergebnis.FormelManager` | |
| `regelwerk/Regelwerk` | `jutils.util.StringTools` | |
| `regelwerk/Regelwerk` | `java.io.ByteArrayOutputStream`, `java.io.PrintStream` | |
| `regelwerk/Strafen` | `gui.penalties.PenaltyUtils` | `PenaltyUtils` does not fit `gui` (`data-gui-print-packages.md`) |
| `veranstaltung/CompetitionContainer` | `java.io.File` | |
| `veranstaltung/Veranstaltung` | `java.nio.file.Path` | |
| `misc/BugReport` | `gui.util.I18n` | `BugReport` may leave `daten` (`daten-only-model.md`) |

Found from the `import` statements; fully qualified uses in code are not covered — check with a grep for
`de.df.` / `java.` outside import lines when working through the list.

## Done when

The table is empty: every reference is either kept with a reason recorded in `AGENTS.md` (as for the persistence
mapping) or removed.
