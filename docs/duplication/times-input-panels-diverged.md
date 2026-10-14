# Times input panels in the checks plugin add swimmers by different rules

`JRegistrationTimesInputPanel` and `JTimesInputPanel` (`jauswertung-plugins-checks`) share about 300 lines (status,
cursor, sorting, list handling). They differ in how an edited swimmer is re-checked:

- `JRegistrationTimesInputPanel.java:362` uses `getRegistrationTimeStatus`, the same rule as its initial fill
  (`:131`).
- `JTimesInputPanel.java:415` uses `hasIncompleteTime` and `TimeStatus.NONE`, while its initial fill (`:155`) uses
  `getTimeStatus`, which includes FAST/SLOW.

After an edit, a swimmer whose time becomes implausible is not added and gets no fast/slow marker.

## Done when

`JTimesInputPanel` re-checks with `getTimeStatus`, ideally through an abstract base panel shared by both classes.
