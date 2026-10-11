# OWDisziplin.Id is stored although it is derived

`OWDisziplin.Id` is always `OWDisziplin.getId(akNummer, maennlich, disziplin, round)`, but it is persisted as its own
element (and once more as the key of `OWLaufliste.disziplinen`). Many callers read the public field `d.Id` directly
(e.g. `HeatsUtils`, `ResultUtils`, `JPenaltyEditor`, `AresWriter`).

## Done when

- The id is computed by a getter and no longer persisted; old files load (the element is omitted or migrated).
- Direct field accesses in all modules use the getter.
- All compatibility years still load and export unchanged.
