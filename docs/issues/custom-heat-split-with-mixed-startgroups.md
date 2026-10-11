# Custom heat split fails for start groups with different discipline counts

## Finding

Generating heats with a non-standard split throws `ArrayIndexOutOfBoundsException` in
`AWettkampf.reorderDisciplines` (`jauswertung-data/.../daten/AWettkampf.java`, marked
`// TODO: Update implementation SG`) when a start group contains age groups with different numbers of disciplines.

- `Laufliste.erzeugen(Einteilung[], …)` calls `wk.reorderDisciplines(r)` whenever the split is not the standard one.
- `r` is sized per start group from the discipline count of the *first* age group in that group
  (`getAKsForStartgroup(...).getFirst()`).
- `reorderDisciplines` then iterates over the disciplines of *every* age group in the start group and reads
  `verteilung[sg][z]` beyond the array for an age group with more disciplines.

## Reproduction

With the single rulebook DLRG 2026: put `AK 13/14` (3 disciplines) and `AK 15/16` (6 disciplines) into the same start
group, then create heats in the wizard with a custom split ("Einteilung" or "Blocks") whose order differs from the
default.

Found while writing `jauswertung-misc/.../misc/ErzeugeKompatibilitaetsWettkaempfe.java`, which now avoids the case by
only grouping age groups with equal discipline counts.

## Done when

A custom split works for start groups whose age groups have different discipline counts, covered by a test in
`jauswertung-data` or `jauswertung-io`.
