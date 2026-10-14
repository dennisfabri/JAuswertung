# Undo/redo uses Java serialization

`MUndoPlugin` (`jauswertung-plugins-undoredo`) keeps up to 20 snapshots of the whole competition for undo and as many
for redo. Each snapshot is written with Java serialization (`OutputManager.serializeObject`, `ObjectOutputStream`)
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
- 40 full, uncompressed copies of a large competition are kept in memory.

## Decide

Which mechanism undo/redo should use, measured with a large competition (time per step, memory for 20 steps):

- Java serialization as today, with errors reported instead of swallowed.
- XStream via the existing `OutputManager.serialize` / `InputManager.unserialize`, optionally compressed — the same
  path as the files, so undo also covers the file format.
- Deep copies with `Utils.copy`, keeping object graphs instead of bytes.

Then implement the choice, make a failed snapshot or restore visible to the user, and correct the statement in
`AGENTS.md`. If Java serialization is dropped, the `serialVersionUID`s in the model can go as well.
