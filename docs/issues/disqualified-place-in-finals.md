# Disqualified swimmers get a place in finals

## Finding

In the final of a discipline with several rounds, a disqualified swimmer keeps a place: `FormelILSFinals` ranks them
after all valid results and only removes the place in the first round or outside finals (`setPointsDirect` and
`setPoints`, branch `DISQUALIFIKATION`). Their points are correct: `FormelILS.getPoints` gives the points of the last
place of their group (`getPoints(rank + amount - 1)`).

Example from `compatibility/2025/Finale-Einzel.wk`, Board Race final, women:

| Place | Swimmer | Points | Penalty |
| --- | --- | --- | --- |
| 1 | Leconte | 20 | |
| 2 | Gamble | 18 | |
| 4 | Schatz | 14 | disqualified |
| – | Siggins | – | did not start |

Swimmers who did not start already get no place and no points in every round.

## Done when

- A disqualified swimmer in a final gets no place but keeps the points they get today (14 for Schatz above).
- Exclusion (`AUSSCHLUSS`) and disqualification in earlier rounds keep their current behaviour.
- The rank is still used to calculate the points, so removing the place must happen after the points are set.
- The finals snapshots in `jauswertung-io/src/test/resources/compatibility/` are updated for the changed rows.
