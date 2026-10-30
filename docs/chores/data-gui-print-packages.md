# gui and print in jauswertung-data hold more than data classes

*Packages in jauswertung-data* (`AGENTS.md`) allows only data classes for scoped subdomains (DTOs, temporary models)
in `de.df.jauswertung.gui` and `de.df.jauswertung.print` of this module. Current content (paths relative to
`jauswertung-data/.../jauswertung/`):

| Class | Fits? |
| --- | --- |
| `gui/util/LaufInfo` | Yes — heat and lane as strings |
| `gui/util/TimeStatus` | Yes — enum |
| `print/Zieleinlaufkarte` | Yes — values of a finish card, read from a `Lauf` |
| `gui/util/SchwimmerInfo` | Check — data class, but `getHeatDifference` and `isSimilar` carry comparison logic |
| `gui/util/SchwimmerUtils` | No — utility; leaves `gui.util` (`data-without-swing-and-print.md`) |
| `gui/penalties/PenaltyUtils` | No — utility that formats penalty texts; also referenced from the model (`Strafen`) |
| `gui/util/I18n` | No — text lookup utility; *Modules* names it as part of this module, so its place needs a decision |

## Done when

Only data classes remain in both packages; utilities have moved to a fitting package (`util` once it is classified,
see `examine-data-util.md`), and the decision about `I18n` is recorded in `AGENTS.md`.
