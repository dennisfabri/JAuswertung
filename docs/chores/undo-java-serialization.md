# Undo/redo uses Java serialization

`MUndoPlugin` (`jauswertung-plugins-undoredo`) keeps up to 20 snapshots of the whole competition plus the current
state. Undo and redo only move snapshots between their two lists, and every new change clears the redo list, so there
are never more than 21 in total. Each snapshot is written with Java serialization (`OutputManager.serializeObject`, `ObjectOutputStream`)
into an uncompressed `byte[]` and read back with `InputManager.unserializeObject`. It is the only use of Java
serialization in the application; files are written with XStream, and in-memory copies (`Utils.copy`) use a deep
cloner.

Problems with the current approach:

- `AGENTS.md` (*Persistence and Compatibility*) says autosave and undo reuse the same serialization; they do not.
  Undo neither exercises nor is protected by the XStream aliases, `allowTypes` whitelist and migrations of the file
  format.
- Every class reachable from `AWettkampf` must implement `java.io.Serializable`. A new field of a non-serializable
  type breaks undo without a visible error: `serializeObject` only prints the stack trace and stores what was
  written so far, `unserializeObject` swallows the exception and returns `null`, and `undo`/`redo` then pass `null` to
  `core.setWettkampf`.
- The `serialVersionUID`s of the model classes exist only for this purpose.

## Decision

Undo/redo uses deep copies with `Utils.copy`, keeping object graphs instead of bytes.

- **Speed:** on real competition files today's Java serialization already needs at most 1.2 ms per step, so speed does
  not decide. The deep clone is the fastest candidate already in the repo (about 30–55 % of today's time) and stays
  fast on synthetic competitions with 3000 participants (6 ms).
- **Memory does not decide either:** with at most 21 snapshots, even the largest synthetic competition plus a 985 KB
  logo needs about 75 MB with the deep clone, and real championship files at most 5 MB. That is below the 100 MB this
  feature typically reaches, and `global.vmoptions` sets no `-Xmx`, so the JVM default heap (a quarter of physical
  memory) leaves ample room.
- **Robustness decides:** the deep clone needs no `Serializable`, does not hide failures in byte arrays, needs no new
  dependency, and is the mechanism already used for in-memory copies (`Utils.copy`, e.g. in
  `OutputManager.speichereWettkampf`).
- XStream is ruled out (up to 180 ms per snapshot and 290 ms per restore at 3000 participants, on every change);
  covering the file format stays the job of save and autosave. Kryo and Fory give no advantage over the deep clone
  and would be new dependencies.

## Done when

- `MUndoPlugin` stores `Utils.copy(core.getWettkampf())` per snapshot and hands a fresh `Utils.copy` of the snapshot to
  `core.setWettkampf` on undo and redo, since the snapshot stays in the list.
- A failed snapshot or restore is reported to the user instead of passing `null` on.
- The transient fields of the model (`Lauf.usable`, `Lauf.leftToRight`, `Laufliste.isfinal`,
  `AWettkampf.disciplinesComplete`, `Altersklasse.interneStartgruppe`) are checked: the clone copies them, whereas the
  serialization reset them and ran the `readResolve` hooks. Undo must restore a consistent state either way.
- The statement about undo in `AGENTS.md` (*Persistence and Compatibility*) is corrected.
- `OutputManager.serializeObject` and `InputManager.unserializeObject` go away. Bug reports are written with XStream,
  but `InputManager.ladeBugReport` still tries Java serialization first, for old reports; decide whether those still
  need to load before removing the `serialVersionUID`s of the model.
- Optional: keep the logo once instead of in every snapshot (it is never changed in place; `setLogo` replaces the
  array). Only worth it if logos well above 1 MB become common.

## Measurements (2026-10-30)

`save()` takes a full snapshot on every data change — including every single time entry — synchronously in
`dataUpdated`; `undo`/`redo` restore from a snapshot that stays in the list, so a restore needs a fresh graph, too.
Snapshot time is therefore the number that matters most.

Throwaway benchmark (not in the repo): snapshot and restore exactly as each candidate would do them, median of 15 runs
after 8 warm-up runs, JDK 25, Intel Core Ultra 9 285H. Every round trip was checked against the original (XStream XML
of both, compared line by line ignoring the order of `HashMap` entries and XStream's `reference="…"` paths, which
change with that order); all candidates passed. Competitions:
`DSM2024_Einzel.wk` (281 participants) and `DSM2024_Mannschaft.wk` (134 teams) from `jauswertung-misc/src/test/resources`,
plus two generated with `ErzeugeWettkaempfe` (3000 participants, 3000 teams). Both DSM2024 files contain a 673 KB logo,
which is part of every snapshot and makes up most of their memory figures.

Each cell is snapshot / restore / memory for 20 snapshots, relative to today's Java serialization (100 %; its row
shows the absolute values). Below 100 % is better. Memory of byte-based methods is 20 × snapshot size; for the deep
clone it is the measured heap growth, which is coarse (~). With baselines around 1–2 ms, a few tenths of a
millisecond of noise already move the percentage a lot.

| Method | DSM2024 Einzel | DSM2024 Mannschaft | 3000 Einzel | 3000 Mannschaft |
| --- | ---: | ---: | ---: | ---: |
| Java serialization (today) | 2.9 ms / 1.6 ms / 15.6 MB | 1.0 ms / 1.2 ms / 15.7 MB | 12.5 ms / 8.1 ms / 22.6 MB | 20.1 ms / 12.8 ms / 34.9 MB |
| Java serialization + Deflate level 1 | 376 % / 156 % / 88 % | 1090 % / 208 % / 87 % | 82 % / 131 % / 20 % | 76 % / 128 % / 18 % |
| XStream (`OutputManager.serialize`) | 703 % / 1875 % / 197 % | 1410 % / 1717 % / 201 % | 762 % / 2309 % / 580 % | 770 % / 2220 % / 664 % |
| XStream + ZIP (file and autosave path) | 1166 % / 1494 % / 89 % | 3410 % / 1975 % / 89 % | 908 % / 2343 % / 24 % | 901 % / 2237 % / 23 % |
| Deep clone (`Utils.copy`) | 31 % / 56 % / ~113 % | 40 % / 33 % / ~106 % | 30 % / 54 % / ~190 % | 28 % / 50 % / ~158 % |
| Kryo 5.6 (not a dependency) | 31 % / 50 % / 91 % | 60 % / 42 % / 92 % | 33 % / 30 % / 46 % | 32 % / 32 % / 52 % |
| Fory 0.12 (not a dependency) | 28 % / 56 % / 94 % | 40 % / 42 % / 94 % | 38 % / 27 % / 61 % | 36 % / 20 % / 62 % |

Single runs without JMH; differences below ~2 ms are noise (e.g. Deflate measuring faster than plain Java
serialization on one file).

Observations:

- **XStream is out for undo**: 95–180 ms per snapshot and up to 290 ms per restore at 3000 participants, on every
  change. That would make time entry noticeably sluggish. Covering the file format is already the job of save and
  autosave.
- **Deep clone is the fastest candidate already in the repo** (3–4× faster than today), needs no `Serializable` and
  no bytes. It copies `transient` fields as well, while every byte-based method resets them; Java serialization and
  XStream then run the `readResolve` hooks (`Laufliste` restores `isfinal`, `AWettkampf` drops old `Hlw` properties),
  the clone does not need them. Undo would restore exactly the state before the change; the transient caches
  (`Lauf.usable`, `AWettkampf.disciplinesComplete`, `Altersklasse.interneStartgruppe`) must be checked for that.
  Memory is the highest apart from XStream, ~55 MB for 20 steps at 3000 teams.
- **Java serialization + Deflate** uses the least memory (5–6 MB for 20 steps at 3000 participants) at 10–16 ms per
  snapshot; on small competitions the compression costs more than the serialization itself.
- **Kryo and Fory** are about as fast as the deep clone but are new dependencies, and Fory needs `--add-opens` for
  `java.base`, which `global.vmoptions` does not have (the benchmark added them; not tested without). Kryo with the
  settings used does not call `readResolve`, so `Laufliste.isfinal` would come back `false`. Neither gives an
  advantage over `Utils.copy`.

### Generated test competitions and a logo

Second run, same benchmark without XStream, with the generated competitions from `data/` (`ErzeugeWettkaempfe`):
small `Einzel 1.wk` (120 participants), medium `Mannschaft 2.wk` (320 teams), big `Mannschaft 3.wk` (640 teams). The
medium one was also measured with a logo: a 2400×1600 JPEG of 985 KB set with `setLogo`, which keeps it as a `byte[]`
in the properties — so it is part of every snapshot.

Same layout as above:

| Method | Small | Medium | Medium + logo | Big |
| --- | ---: | ---: | ---: | ---: |
| Java serialization (today) | 1.4 ms / 1.6 ms / 1.4 MB | 1.1 ms / 1.6 ms / 4.3 MB | 2.5 ms / 1.8 ms / 23.5 MB | 3.0 ms / 3.8 ms / 7.9 MB |
| Java serialization + Deflate level 1 | 50 % / 56 % / 21 % | 164 % / 131 % / 19 % | 732 % / 339 % / 85 % | 110 % / 100 % / 19 % |
| Deep clone (`Utils.copy`) | 36 % / 31 % / ~157 % | 55 % / 44 % / ~147 % | 28 % / 44 % / ~110 % | 37 % / 34 % / ~154 % |
| Kryo 5.6 | 21 % / 25 % / 43 % | 64 % / 44 % / 53 % | 52 % / 33 % / 91 % | 43 % / 24 % / 53 % |
| Fory 0.12 | 21 % / 31 % / 57 % | 36 % / 31 % / 63 % | 32 % / 28 % / 93 % | 23 % / 16 % / 62 % |

Observations:

- At these sizes every candidate stays below 4 ms per step; the differences are mostly noise.
- **The logo dominates memory for every method**: 20 copies of it add about 19–20 MB, more than the whole big
  competition. Compression does not help, since a JPEG is already compressed — Deflate then costs 18 ms per snapshot
  for 4 % less memory.
- The logo is never changed in place (`setLogo` replaces the array). Whatever mechanism is chosen could therefore keep
  one shared logo instead of 20 copies — with the deep clone by not cloning that array, with byte-based methods by
  taking the logo out of the snapshot and putting it back on restore (as `OutputManager.speichereWettkampf` already
  does for files). That removes the logo from the memory question entirely.

### DEM2026 and DOM2026

Third run, same benchmark and layout, with the competitions of DEM2026 and DOM2026 from `data/` (without their
autosave copies). Participants or teams in parentheses.

| Method | DEM Einzel Vorläufe (317) | DOM Einzel (269) | DEM Mannschaft Vorläufe (70) | DEM Mannschaft Final (56) | DOM Mannschaft (55) |
| --- | ---: | ---: | ---: | ---: | ---: |
| Java serialization (today) | 0.8 ms / 1.2 ms / 3.1 MB | 0.6 ms / 1.0 ms / 2.5 MB | 0.4 ms / 0.8 ms / 2.1 MB | 0.4 ms / 0.7 ms / 1.9 MB | 0.3 ms / 0.6 ms / 1.9 MB |
| Java serialization + Deflate level 1 | 175 % / 125 % / 23 % | 183 % / 130 % / 21 % | 200 % / 125 % / 18 % | 200 % / 129 % / 18 % | 267 % / 133 % / 22 % |
| Deep clone (`Utils.copy`) | 50 % / 42 % / ~162 % | 50 % / 40 % / ~189 % | 75 % / 38 % / ~127 % | 50 % / 29 % / ~128 % | 67 % / 33 % / ~117 % |
| Kryo 5.6 | 75 % / 33 % / 48 % | 50 % / 30 % / 46 % | 50 % / 25 % / 47 % | 50 % / 29 % / 46 % | 67 % / 33 % / 44 % |
| Fory 0.12 | 62 % / 58 % / 63 % | 33 % / 20 % / 60 % | 25 % / 25 % / 58 % | 25 % / 29 % / 62 % | 33 % / 17 % / 65 % |

The four small ones — DEM Einzel Final (6), DEM Mixed Final (6), DEM Mixed Vorläufe (14), DOM Mixed (17) — take at
most 0.4 ms per snapshot or restore with every method and 0.1–0.6 MB for 20 snapshots; at 0.0–0.1 ms percentages
would only show noise.

Observations:

- Real championship files are small for undo: at most 1.2 ms per step and 3 MB for 20 steps today. Every candidate
  is fast enough; the relative order is the same as in the larger runs.
- These files exposed a flaw in the first version of the round-trip check (map order changes XStream's reference
  paths, so it reported false differences). After excluding the reference lines, every candidate restores every file
  exactly.
