# Sex labels are looked up by hand with different fallbacks

`I18n.geschlechtToString`/`…Subject`/`…ShortString` (`jauswertung-data/.../I18n.java:121-173`) translate with German
defaults. Hand-written lookups with other fallbacks:

- `TimeLimitsPlugin.java:78-82` — English defaults (`"Female"`, `"female"`, `"f"`).
- `AresWriterFinals.java:55,388` — hard-coded `"m"`/`"w"`.
- `VeranstaltungsUtils.java:48-49,69-70`, `ImportUtils.java:1110-1119`, `AKsSexesPanel.java:86-91`.

## Done when

All of them use the `I18n` helpers (with a helper added for the capitalised form used in `TimeLimitsPlugin`).
