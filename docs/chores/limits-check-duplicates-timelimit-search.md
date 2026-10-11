# LimitsCheck duplicates the search for broken time limits

`jauswertung-misc/.../misc/LimitsCheck.java` is a developer tool with its own copy of the search for broken time
limits (`checkRecords`). The app now has that search once, in `TimelimitUtils` (`jauswertung-data`), used by the
printout and by the check in the time limits menu.

## Done when

`LimitsCheck` uses `TimelimitUtils.hasBrokenTimelimits`/`getBrokenTimelimits` instead of its own loop, or is removed
if the tool is no longer needed.
