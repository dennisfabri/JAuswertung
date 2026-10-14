# Event-scoring results prepare their input twice

`VeranstaltungsUtils.veranstaltung2Wettkampf` builds the scored competition. `ResultsPrinter.getGesamtwertungPrintable`
(`jauswertung-veranstaltungswertung/.../ResultsPrinter.java:87-170`) loads every competition file again and repeats the
filtering by qualification level to count the age groups per competition for the header row with the competition
names. A third copy of the filter is in `ResultsPrinter.veranstaltung2Printable` (`:297-335`), which belongs to the
"normal" view; `resultcompact` is always selected and hidden, so that view cannot be reached.

The re-loaded files are not the data that was scored, so the competition names can span the wrong columns:

- out-of-competition swimmers are not removed in the copy,
- the compact printout passes `gliederung = true` (`:205`) even when printing by qualification level,
- the compact printout splits by sex, but counts age groups of both sexes,
- the table drops leading age groups without points (`TableUtils`, `min`), the count always starts at the first.

## Done when

`veranstaltung2Wettkampf` hands out the competition name of each age group it builds, the printer derives the header
row from it without loading the files again, and the unreachable "normal" view is removed.
