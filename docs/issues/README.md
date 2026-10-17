# Open Issues

Open findings from code work that are not fixed yet. Each issue is a self-contained file; once it is fixed and
verified, delete the file and its row here (see `AGENTS.md` → *docs/issues/*). Keep this README with its table even
when no issues are open.

Every row has a severity and a size; keep the table ordered by severity (High, Medium, Low), then by size (S, M, L).

- **Severity:** High = wrong results or lost/corrupted data; Medium = a feature fails or misleads, with a workaround;
  Low = no user-visible effect, or only inconvenience.
- **Size:** S = a local change of a few lines with a test; M = several classes or a migration; L = spread over many
  modules or needs a design decision.

| Issue | Severity | Size | Summary |
| --- | --- | --- | --- |
| [Import wizard keeps the previous result after a failed import](import-wizard-keeps-old-result-after-error.md) | Medium | S | Several error handlers do not reset `results`, so Next imports the data of the previous file |
| [Event scoring does not warn about unsaved competition changes](event-scoring-competition-changes-not-modified.md) | Medium | S | The competitions panel and the text fields never call `setModified()`, so closing discards changes silently |
| [Team member scan never shows the "Names" signal](teammember-scan-partial-signal-unreachable.md) | Low | S | The `else if` repeats the first condition, so the orange signal is unreachable |
| [Export button of the per-discipline printers is always disabled](per-discipline-export-button-always-disabled.md) | Low | S | `hasExport()` is false everywhere; Excel export of a round is unreachable |
| [Event scoring shows a wrong save error](event-scoring-save-error-text.md) | Low | S | Missing key `SaveFailed.Note` and the old file name in the failed-save dialog |
| [Print previews read Swing widgets off the EDT](print-preview-reads-widgets-off-edt.md) | Low | M | 20 preview creators read widgets in the background; mitigated because the preview blocks its parent |
| [Development mode features belong in the beta plugin](development-mode-to-beta-plugin.md) | Low | L | Features and diagnostics behind the `development` preferences node should be switched by `OBetaPlugin` |
