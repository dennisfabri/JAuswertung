# Event scoring does not warn about unsaved competition changes

In the Veranstaltungswertung, only `JVeranstaltungseinstellungen` calls `JVeranstaltungswertung.setModified()` (`:89`).
`JCompetitionCollector` never does: adding, editing or removing a competition leaves `modified` false, and the text
fields of the properties panel (name, title, location) do not mark it either. Closing the window then exits without the "unsaved changes" question and the changes
are lost.

## Done when

Every change in the competitions panel and the text fields of the properties panel marks the event as modified, so
closing asks before discarding it.
