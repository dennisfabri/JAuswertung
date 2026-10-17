# Event scoring shows a wrong save error

`JVeranstaltungswertung.speichereVeranstaltung(String)` (`jauswertung-veranstaltungswertung`, `:243-244`) reports a
failed save with `I18n.get("SaveFailed.Note", filename)`. The key does not exist (the text is `SaveFailedText.Note`), so
the dialog shows the raw key. Both texts also use the field `filename` — the previous file name, or `null` for a new
event — instead of the parameter `name` that failed to save.

## Done when

The dialog uses `SaveFailedText.Note` and names the file that could not be written.
