# PrinterCollection exists twice

`jauswertung-plugins-print/.../PrinterCollection.java:23-91` and
`jauswertung-veranstaltungswertung/.../PrinterCollection.java:21-64` are the same container for different `Printer`
interfaces: same fields, constructor, `add`, `getPanel` and `getName`. They differ only in the layout method (two
columns in the print plugin, one column in the event scoring) and in `dataUpdated`, which only the print plugin's
`Printer` interface declares.

## Done when

A generic container in `jauswertung-print` serves both.
