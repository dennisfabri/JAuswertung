# Discipline values are stored per gender

`Altersklasse` keeps its disciplines as `Disziplin[][]`, one array per gender (`[0]` female, `[1]` male). `name`,
`laenge` and `laps` are therefore stored twice, although the rulebook editor writes one value per row into both
(`DisziplinenPanel`) and `Altersklasse.isValid()` requires the names to match. Only `rec`, `runden` and `rundenIds`
really differ by gender.

## Decision

Kept on purpose (2026-10-30): name, length and laps stay per gender.

## Done when

- Name, length and laps exist once per discipline; record, rounds and round ids stay per gender.
- A migration converts competitions (`.wk`) and rulebooks (`.rwe`/`.rwm`, including the shipped ones in
  `jauswertung-files/src/main/resources/aks`).
- All compatibility years under `jauswertung-io/src/test/resources/compatibility/` still load and export unchanged.
