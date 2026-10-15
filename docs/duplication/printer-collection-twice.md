# PrinterCollection exists twice

`jauswertung-plugins-print/.../PrinterCollection.java:23-91` and
`jauswertung-veranstaltungswertung/.../PrinterCollection.java:22-90` are the same container for different `Printer`
interfaces (CPD 380 tokens).

## Done when

A generic container in `jauswertung-print` serves both.
