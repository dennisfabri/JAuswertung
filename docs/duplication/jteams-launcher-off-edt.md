# JTeams launcher builds its UI on the main thread

`jauswertung/.../gui/JAuswertungLauncher.java:123-136` creates the `PluginManager`, the window and calls `japm.start`
inside `SwingUtilities.invokeLater`. The copy `jteams/.../gui/JAuswertungTeammembersLauncher.java:89-102` does the same
on the main thread. Command-line handling and initialisation are copied as well (CPD 153 and 124 tokens).

## Done when

Both launchers use one helper (e.g. in `jauswertung-plugins-core` or `jauswertung-swing`) parameterised with app name,
plugin directory and window key, which starts the UI on the EDT.
