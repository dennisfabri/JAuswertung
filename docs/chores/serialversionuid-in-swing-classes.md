# serialVersionUID in Swing classes

Swing classes declare a `serialVersionUID` only because `java.awt.Component` is `Serializable` and IDEs warn about the
missing field. Swing components are never serialized: files are written with XStream, which ignores the field, and
the only Java serialization (the undo snapshots in `MUndoPlugin`) writes the model (`AWettkampf`), not UI objects, and
reads it back in the same JVM. The build does not enable `-Xlint:serial`.

Usage is inconsistent: about 77 files in the `gui`, `print` and `swing` packages declare the field, many other Swing
classes (and most anonymous renderers and panels) do not.

Model classes (`de.df.jauswertung.daten`) and exceptions keep their `serialVersionUID`; they are not part of this
chore.

## Done when

Swing classes (frames, dialogs, panels, renderers, actions, including anonymous and inner classes) no longer declare a
`serialVersionUID`, and no `@SuppressWarnings("serial")` is added in their place.
