# Medal list per discipline hides the organisation column the wrong way round

`ListOfMedalsPerDisciplinePrinter.java:286` passes `printOmitOrganisationForTeams && wk instanceof MannschaftWettkampf`
into the parameter `showOrganisation` of `getPseudoTable`; `:293` then picks the layout that gives the organisation
column `0dlu` when `!showOrganisation`. The organisation is thus hidden by default and shown only when "omit for teams"
is set on a team competition.

`getPseudoTable` (about 86 lines, CPD 587/423 tokens) exists three times, each different:

- `ListOfMedalsPerDisciplinePrinter.java:291` — inverted, see above.
- `ListOfMedalsPrinter.java:183-185` — `if (PrintUtils.printOmitOrganisationForTeams)`: right direction, but ignores
  "for teams".
- `SiegerlistePrinter.java:122` — always shows the column.

Derived from the layout strings; not yet confirmed in a print preview.

## Done when

One `getPseudoTable(etm, omitOrganisation, big)` in `jauswertung-print` is used by all three printers with
`omit && isTeam`, checked in the print preview.
