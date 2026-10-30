# Module rules are enforced only by convention

Found by the Architecture review of `jauswertung-data` (`docs/reviews/README.md`). *Dependency Rules* in `AGENTS.md`
is only prose; nothing fails the build when the model imports Swing or printing or a lower module gains a dependency
on a higher one. `jauswertung-data` is currently free of both apart from the exceptions below.

## Done when

- ArchUnit tests forbid `javax.swing..`, `java.awt..`, `javax.print..`, `de.df.jutils.gui..`, `de.df.jutils.print..`
  and `de.df.jutils.plugin..` in `jauswertung-data`, with exactly two allowed exceptions: `I18n.getKeyStroke`
  (`javax.swing.KeyStroke`) and the persistence mapping (XStream, commons.xmt) — see *Dependency Rules*.
- A Maven Enforcer `bannedDependencies` rule keeps `jutils-print` and `jutils-swing` out of `jauswertung-data`.
- Both run in `mvn verify`.
