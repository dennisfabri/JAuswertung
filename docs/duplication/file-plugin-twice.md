# MFilePlugin exists twice

`jauswertung-plugins-file/.../MFilePlugin.java:116-330` and `jteams/.../teammembersinput/MFilePlugin.java:122-319`
are largely the same (CPD 508 + 263 + 137 tokens).

The jteams `openFile(String)` (recent files / command line, `:186-196`) does not call
`FileChooserUtils.setBaseDir(dir)` after a successful load; the main copy does (`:195-210`). The enabling of the save
icons differs as well.

## Done when

Both use a base `AFilePlugin` in `jauswertung-plugins-core`, configured with the file filters and whether a new file
may be created.
