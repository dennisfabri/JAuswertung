# JHeatPresentationFrame exists twice

`jauswertung-dp-extensions/.../displaytool/JHeatPresentationFrame.java` (440 lines) and
`jauswertung-plugins-heats/.../heatsview/JHeatPresentationFrame.java` (469 lines) share the full-screen shell, key
handling, cursor and painter thread (CPD 256/159/135 tokens).

Only the dp copy has the Linux `IndexOutOfBoundsException` catch around the default cursor, the R key restore, a
daemon painter thread and image re-creation on size change.

Both check `screenIndex > devices.length` (dp `:222`, heats `:218`); it must be `>=`, otherwise a screen index equal
to the number of screens throws `ArrayIndexOutOfBoundsException`.

## Done when

The off-by-one is fixed and both frames extend an `APresentationFrame` in `jauswertung-swing` with an abstract
`paintContent(Graphics2D, w, h)`.
