# FormelLinear has drifted from FormelDLRG2007

`FormelDLRG2007.java:118-386`, `FormelLinear.java:111-387` and `FormelDirectPoints.java:59-327` are copies of the
same `setPoints`/`toResults` logic (CPD: 900 and 890 tokens). `FormelLinear` is selectable (`FormelManager.java:43`)
and differs:

- General penalties: DLRG2007 (`:144-145`) uses
  `new Strafe(getAkkumulierteStrafe(SELF), getAK().isStrafeIstDisqualifikation())`, Linear (`:137`) only
  `getAkkumulierteStrafe(SELF)`. Linear ignores the age-group setting "penalty counts as disqualification".
- "All chosen disciplines did not start → no ranking" (`na == getDisciplineChoiceCount()`, DLRG2007 `:233-244`,
  DirectPoints `:174-180`) is missing in Linear (`:203-262`); Linear does not count `DISQUALIFIKATION` towards `na`
  either.

`double count` (DirectPoints/Linear) vs. `int count` (DLRG2007) is a harmless difference.

## Done when

Linear has tests for both rules and behaves like DLRG2007, preferably by removing the copy (see
[Scoring formula bodies are copied](scoring-formula-bodies.md)).
