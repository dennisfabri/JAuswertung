# jauswertung-data depends on Swing and printing

Found by the Architecture review of `jauswertung-data` (`docs/reviews/README.md`). *Dependency Rules* says the domain
model must not depend on Swing panels, plugins, or I/O formats, but `jauswertung-data/pom.xml:20` depends on
`jutils-print`, which pulls in `jutils-swing` and `jutils-io`, and the model uses them (paths below are relative to
`jauswertung-data/src/main/java/de/df/jauswertung/`):

- `gui/util/SchwimmerUtils.java:345-356`: `checkTimeAndNotify(Window parent, …)` beeps through `Toolkit` and asks the
  user with `DialogUtils.askAndWarn`. Callers: `JTimeEditor`, `JMeanTimeEditor` (plugins-editor).
- `daten/misc/BugReport.java:73-80`: collects fonts and printers through `PrintManager`, `java.awt` and `javax.print`.
  `BugReport` itself is saved under the XStream alias `BugReport` (`IOUtils:205`), so only the collecting code may move
  — the class must keep its alias, or old bug reports no longer load.
- `util/CompetitionUtils.java:99-164`: `generateMedaillenspiegel` returns a Swing `TableModel` built by its own
  `MedalTableModel extends AbstractTableModel`. Caller: `MedaillenspiegelPrinter` (plugins-print).
- `util/Utils.java:150-160`: `getUIPerformanceMode`/`setUIPerformanceMode` use jutils-swing's `UIPerformanceMode`.
  Callers: `PluginApplicationLauncher` (plugins-core), `Launcher`, `UIModeWindow` (jauswertung).

Kept on purpose (not part of this chore): `I18n.getKeyStroke` returning `javax.swing.KeyStroke`, and the model's own
persistence mapping (XStream annotations, commons.xmt `VersionedDocument`) — see *Dependency Rules*.

## Done when

- `checkTimeAndNotify` lives in `jauswertung-swing`; the plausibility check it wraps stays in the model.
- The font and printer collection for bug reports lives in `jauswertung-print` or higher; `BugReport` keeps only data.
- `generateMedaillenspiegel` returns plain values (e.g. a list of records); the `TableModel` is built in
  `jauswertung-swing` or plugins-print.
- The UI performance mode getter and setter live in `jauswertung-swing`.
- `SchwimmerUtils` has no Swing relation left and moves out of the `gui.util` package (e.g. to `util`).
- `jutils-print` is removed from `jauswertung-data/pom.xml`. `jauswertung-swing` currently gets `jutils-swing` only
  transitively through data; declare it there directly.
