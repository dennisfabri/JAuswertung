# Replace filechooser

`org.lisasp.swing:filechooser-*` (core, awt, jfx, dbus, l2f) is a hand-maintained library for native file dialogs. grep
finds 21 Java files using `org.lisasp.swing.filechooser`.

Check whether a maintained library or the JDK/JavaFX file choosers cover the platforms JAuswertung supports. If not,
decide whether to keep it as an own, properly released project.

## Done when

The dependency is replaced, or the decision to keep it as an own project is recorded here and the chore is deleted.
