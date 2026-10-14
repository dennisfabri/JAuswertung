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
| [LimitsCheck duplicates the search for broken time limits](limits-check-duplicates-timelimit-search.md) | Low | S | `LimitsCheck` in misc has its own copy of the search for broken time limits |
| [OWDisziplin.Id is stored although it is derived](owdisziplin-id-derived.md) | Low | M | `OWDisziplin.Id` is stored although it is derived from its components |
| [Discipline values are stored per gender](discipline-values-per-gender.md) | Low | L | `Disziplin` stores name, length and laps per gender although they are always equal |
| [Replace printStackTrace() and System.out/System.err](console-output-and-printstacktrace.md) | Low | L | 166 `printStackTrace()` and 169 `System.out`/`System.err` calls bypass logging and error handling |
| [serialVersionUID in Swing classes](serialversionuid-in-swing-classes.md) | Low | L | Swing classes declare a `serialVersionUID` although they are never serialized |
