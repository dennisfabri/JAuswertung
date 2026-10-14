# The penalty list printable exists twice

`jauswertung-print/.../PrintableCreator.java:47-68` and `jauswertung-plugins-print/.../PenaltylistePrinter.java:215-240`
build the same printable (CPD 150 tokens).

The protocol path calls `PenaltyUIUtils.getPenalties(wk, kurz)`, which hard-codes `ignoreNA = false`; it ignores
`PrintUtils.printDidNotStart` and the filter suffix in the title. The penalty list printer respects both.

## Decide

Whether the protocol should always list did-not-start. Then let `PenaltylistePrinter` and the protocol call one
parameterised builder in `jauswertung-print`.
