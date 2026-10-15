# Chores

Planned clean-ups of the code or data model that are not bugs. Each chore is a self-contained file listed in the
table below; once it is done, delete its file and its row here (same rule as `docs/issues/`).

Every row has a severity and a size; keep the table ordered by severity (High, Medium, Low), then by size (S, M, L).

- **Severity:** High = the current state loses data or user settings; Medium = it invites bugs (duplicated or
  inconsistent data); Low = only redundancy.
- **Size:** S = a local change of a few lines with a test; M = several classes or a migration; L = spread over many
  modules or needs a design decision.

| Chore | Severity | Size | Summary |
| --- | --- | --- | --- |
| [Saved block split is never restored](heat-split-blocks.md) | High | S | Saved block split is never restored; fix or remove it |
| [Referees are keyed by the position name](referees-keyed-by-position-name.md) | Medium | M | Referees of a unit are kept in a map keyed by the position name |
| [Undo/redo uses Java serialization](undo-java-serialization.md) | Medium | M | Undo snapshots use Java serialization, unlike the files, and swallow errors |
| [A scoring group is turned into an age group in three places](scoring-group-as-age-group-three-times.md) | Medium | M | `getResultWettkampf`, `generateWertungsgruppenwettkampf` and `generateEinzelwertungswettkampf(wgname)` share their steps; investigate before merging them |
| [Remove the Mersenne Twister](remove-mersenne-twister.md) | Low | S | commons-math3's `MersenneTwister` adds nothing over a JDK generator and is shared unsynchronized in `Laufliste` |
| [OWDisziplin.Id is stored although it is derived](owdisziplin-id-derived.md) | Low | M | `OWDisziplin.Id` is stored although it is derived from its components |
| [Review which penalties are printed and how](penalty-list-review.md) | Low | M | Penalty list: which penalties it contains, what a card shows, and a more compact table layout |
| [Replace swingfx](replace-swingfx.md) | Low | M | Hand-maintained fork of a dead library; used in three files |
| [Replace uistate](replace-uistate.md) | Low | M | Hand-maintained fork of a dead library for window and component state |
| [Replace swingbits](replace-swingbits.md) | Low | M | Hand-maintained fork of a dead library; inline the used parts or use plain Swing |
| [Replace commons.xmt](replace-commons-xmt.md) | Low | M | Hand-maintained fork of a dead library, pinned to a SNAPSHOT; needs a design decision |
| [Check nrjavaserial](check-nrjavaserial.md) | Low | M | Slowly maintained RXTX fork; evaluate jSerialComm with real hardware |
| [Replace System.out/System.err with logging](replace-system-out-with-logging.md) | Low | M | 118 lines in 34 files print to the console; they reach the log only through the stream redirect, without logger name or level |
| [Replace l2fprod-common](replace-l2fprod-common.md) | Low | M | Button bar, tip of the day, status bar and look-and-feel tweaks are replaced; task pane, font chooser and one cell renderer remain |
| [Discipline values are stored per gender](discipline-values-per-gender.md) | Low | L | `Disziplin` stores name, length and laps per gender although they are always equal |
| [Replace printStackTrace()](replace-printstacktrace.md) | Low | L | 166 `printStackTrace()` calls bypass logging and error handling; often the failure is swallowed |
| [serialVersionUID in Swing classes](serialversionuid-in-swing-classes.md) | Low | L | Swing classes declare a `serialVersionUID` although they are never serialized |
| [Replace filechooser](replace-filechooser.md) | Low | L | Hand-maintained native file dialog library; 21 files |
| [Replace jgoodies-forms](replace-jgoodies-forms.md) | Low | L | Only sporadic releases; 193 files, optional |
