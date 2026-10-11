# Referees are keyed by the position name

`KampfrichterEinheit` keeps its positions in `positionen` and the referees in a separate `Hashtable` keyed by the
position name, which duplicates `KampfrichterPosition.position`.

- `renamePosition` has to re-key the map by hand.
- Lookups are inconsistent: `removePosition` compares with `equalsIgnoreCase`, `hasPosition`/`getPosition` with
  `equals`.

## Done when

- Each `KampfrichterPosition` holds its own referees; the map is gone.
- A migration moves the map entries of old files into the positions.
- `REFEREES` exports of all compatibility years stay unchanged.
