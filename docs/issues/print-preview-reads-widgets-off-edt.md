# Print previews read Swing widgets off the EDT

jutils `PrintExecutor.preview` hands the `PrintableCreator` to `JPrintPreview`, which calls `create()` in a
`SwingWorker` (`JPrintPreview.java:291-301`) when the preview opens, after page setup and on PDF export
(`:206`, `:422`). Commit 98261395 made every print action read its widget state on the EDT first; the preview actions
were left out. In 21 of the 56 preview calls the creator still reads widgets, against the `AGENTS.md` rule "never read
Swing components inside the lambda".

In `jauswertung-plugins-print/.../gui/plugins/print/` (preview line, widgets read):

- `ResultsPrinter:308` (`discipline`, `unterschrift`), `ResultsWithDetailedFilterPrinter:252` (`diszipline`,
  `unterschrift`), `ZWResultsPrinter:185` (`unterschrift`), `APerDisciplinePrinter:366` (`agegroup`)
- `ListOfMedalsPrinter:216` (`bigPrint`), `ListOfMedalsPerDisciplinePrinter:322` (`agegroup`, `bigPrint`),
  `BrokenLimitsPrinter:214` (`agegroup`), `BrokenRecordsPrinter:310` (`agegroup`, `allRecords`)
- `PenaltylistePrinter:82` (`disziplin`, `kurz`), `GesamtwertungPrinter:170` (`type`)
- `RegistrationPrinter:235`, `ASelectionlistPrinter:293` (`order`), `RegisteredTeamnamesPrinter:175`
  (`organisation`), `WeitermeldungPrinter:242` (`alltimes`)
- `BahnenlistenPrinter:176` (`number`), `LaufeinteilungPrinter:206` (`selection`)
- `FehlermeldekartenPrinter:118`, `ZieleinlaufkartenPrinter:123`, `StartkartenPrinter:131` (`pages`, `etime`)
- `ALauflistenPrinter:200,208` (`comments`), `SprecherlistePrinter:222,230` (`columns`), `AFilledKartenPrinter:226,238`
  (`pages`, plus the fields `allheats`/`minheat`/`maxheat`)

Elsewhere: `JAKsEditor:511` (`getRegelwerkForPrint()` reads the whole editor) and the Veranstaltungswertung
`ResultsPrinter:292` in the normal view (`resultmodus`, the font selection, and `getVeranstaltung()` writes the panel
values into the event).

The print path of each of these already captures the values on the EDT.

## Mitigation

There is no user-visible effect today: `JPrintPreview` disables its parent window, which holds the widgets, until it is
closed (`JPrintPreview.java:169-173`, `ModalFrameUtil.showAsModal`), and nothing changes the widgets programmatically
in the meantime, so every call to `create()` sees the same values. A preview with another parent, or a background
update of a printer panel, would break this.

## Cause

The print and preview listeners and creators are copied into about 40 printers (`PrintActionListener`,
`PreviewActionListener`, `PPrintableCreator`, `MeldelistenPC`, `ListOfMedalsPC`, …), so the fix of 98261395 had to be
repeated per printer and reached only the print side.

## Done when

The preview listeners capture the widget state on the EDT and pass it to the creator, as the print listeners do;
preferably a base printer reads the state once (`captureState()`) and builds the printable from it (`create(state)`)
for both print and preview.
