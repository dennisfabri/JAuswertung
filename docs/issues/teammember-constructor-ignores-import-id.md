# Team member constructor ignores the import id

## Finding

`Mannschaftsmitglied(String vorname, String nachname, Geschlecht geschlecht, int jahrgang, String importId)` does not
assign `importId`. The constructor has no callers today, so nothing is lost yet, but the first caller would silently
drop the id that re-imports rely on.

## Done when

The constructor assigns `importId`, or it is removed.
