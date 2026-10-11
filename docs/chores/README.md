# Chores

Planned clean-ups of the code or data model that are not bugs. Each chore is a self-contained file listed in the
table below; once it is done, delete its file and its row here (same rule as `docs/issues/`).

| Chore | Summary |
| --- | --- |
| [discipline-values-per-gender.md](discipline-values-per-gender.md) | `Disziplin` stores name, length and laps per gender although they are always equal |
| [owdisziplin-id-derived.md](owdisziplin-id-derived.md) | `OWDisziplin.Id` is stored although it is derived from its components |
| [referees-keyed-by-position-name.md](referees-keyed-by-position-name.md) | Referees of a unit are kept in a map keyed by the position name |
| [heat-split-blocks.md](heat-split-blocks.md) | Saved block split is never restored; fix or remove it |
