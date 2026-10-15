# Import wizard keeps the previous result after a failed import

`JImportWizard.JProgressPage.processFiles` (`jauswertung-plugins-importexport/.../importexport/JImportWizard.java`)
stores the import in the field `results`. The handlers for `TableEntryException` and `TableFormatException` reset it to
`null`; the handlers for `TableException`, `NotSupportedException`/`NotEnabledException`, `IOException` and
`OldExcelFormatException`/`LeftoverDataException` (`:558-572`) do not. `ImportRunner` then enables the Next button with
`results != null` (`:536`).

`results` is only cleared when the wizard opens (`setVisible`, `:118`), not when the progress page runs again. So within
one wizard session: import file A successfully, go back, choose file B, and if B fails with one of these exceptions
(e.g. an empty result, a duplicate start number, an unreadable or old Excel file), the error dialog is shown, but Next
stays enabled and the following pages and Finish work with the data of file A.

The same handlers also show their dialog when `display` is false (the user has already left the progress page), unlike
the first two handlers.

## Done when

Every failed run of the progress page leaves `results` at `null`, so Next stays disabled, and all handlers respect
`display`.
