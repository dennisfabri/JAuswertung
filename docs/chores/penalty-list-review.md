# Review which penalties are printed and how

The penalty list (`PrintableCreator.createPenaltiesPrintable`, used by the standalone printout and the protocol) is
built from `PenaltyUIUtils` (`jauswertung-swing/.../gui/penalties/PenaltyUIUtils.java`): one bordered card per
penalty, i.e. per swimmer, discipline and penalty, sorted by heat and lane, three cards per row.

Points to review:

- **Which penalties:** "did not start" follows the print option; everything else is always listed. General penalties
  (not tied to a discipline) appear only when all disciplines are printed. Heat-based competitions get no penalty list
  in the protocol at all (`ProtocolPrintable`).
- **What a card shows:** the short card has heat/lane, name and organisation, age group and discipline, and the penalty
  text, but not the statement (`Strafe.getName()`); the long card adds the statement and repeats each field with a
  label. A general penalty on a short card prints "AK, " with nothing after the comma.
- **Compaction:** a table with one row per penalty (heat/lane, name, organisation, age group, discipline, penalty,
  statement) would fit far more penalties on a page than cards; several penalties of one swimmer could share a row.
- **Code:** `createPenaltyPanelsShort` and `createPenaltyPanelsLong` are near-copies; the sort key is a padded string
  built in `getID`. The same panels serve the on-screen penalty list (with an edit button), so a printed table would
  need its own builder.

## Done when

It is decided which penalties the list contains and in which form, and the printout is built accordingly.
