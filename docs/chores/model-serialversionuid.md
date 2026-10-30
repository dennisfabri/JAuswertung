# The model is Serializable only for old bug reports

Undo/redo no longer uses Java serialization (it keeps deep copies with `Utils.copy`). The only Java serialization
left is `InputManager.ladeBugReport`, which first tries an `ObjectInputStream` so that bug reports written by old
versions still load; new bug reports are written with XStream (`OutputManager.speichereBugReport`) and read by the
fallback to `InputManager.ladeObject`.

That one fallback is why 35 classes in `de.df.jauswertung.daten` still implement `Serializable` and declare a
`serialVersionUID`, and why a new model class "should" do the same.

## Decide

Whether bug reports from versions that wrote them with Java serialization still need to load.

## Done when

Either the Java-serialization branch in `ladeBugReport` is removed together with `Serializable` and the
`serialVersionUID`s of the model, or it is kept and the reason is recorded in `AGENTS.md` (*Persistence and
Compatibility*).
