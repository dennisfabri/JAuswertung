# Development mode features belong in the beta plugin

Development mode is the Java Preferences node `jauswertung/development` (`Utils.isInDevelopmentMode()`,
`isInDevelopmentModeFor(type)` in `jauswertung-data/.../util/Utils.java:111-130`). `isInDevelopmentMode()` is true as
soon as the node exists; `isInDevelopmentModeFor(type)` additionally needs the key `type.toLowerCase()` set to `1`. The
node can only be created from outside the application (`jauswertung-files/src/main/resources/development/`:
`install-prefs.sh` copies `prefs.xml`, `development.reg` does the same on Windows; both set `dsm`, `pluginmanager` and
`switchagegroups`), so the features behind it are invisible to users. All of them should be switched through
`OBetaPlugin` (`jauswertung-plugins-editor/.../gui/plugins/OBetaPlugin.java`, descriptor `de.dm.auswertung.beta.plugin`)
instead.

## What development mode switches today

User features (hidden from users, but meant to be used eventually):

| Where | Switch | Effect | Read when |
| --- | --- | --- | --- |
| `FormelManager:64` (data) | node exists | The non-public formulas DLRG (old), DSV and direct points (`:31,40,42`) appear in the formula selection. Files that already use them load either way: `get(id)` looks in the full map. | Once, in the constructor of the static singleton (`:20`). |
| `Laufliste:817` (data) | `dsm` | Heat generation applies the DSM movements stored in the competition property `DSM_MODE_DATA`. | Every heat generation. |
| `JNewHeatsWizard:60,92` (heats) | `dsm` | The wizard offers the page that edits `DSM_MODE_DATA`. | Static initializer. |
| `BSwitchAgeGroupsPlugin:28` (input) | `switchagegroups` | Quick button that switches age groups. | Static initializer; the button is only created in the constructor, and `JPFrame` collects quick buttons once when the window is built. |
| `JElektronischeZeit:154` (input) | node exists | Source address `dummy` generates test heats instead of reading AlphaServer. | Every load. |

Developer diagnostics:

| Where | Switch | Effect | Read when |
| --- | --- | --- | --- |
| `DefaultInit:45` (swing) | node exists | Installs `CheckThreadViolationRepaintManager` (EDT checks, see `AGENTS.md` → *Swing Threading*). | Once at startup, before any plugin exists. |
| `PluginApplicationLauncher:79` (plugins-core) | `pluginmanager` | Verbose `PluginManager`: logs every update event and slow listeners. | Once at startup, before the plugins are loaded. |
| `BugreportPlugin:39,84` (bugreport) | node exists | `BugReportExceptionHandler` additionally logs every uncaught exception (`:71`); whether the bug-report dialog appears does not depend on it. | At startup and per handled exception. |
| `AboutDialogCreator:25` (info) | node exists | The About dialog gets two extra tabs with system and UI properties. | When the dialog is first created. |
| `MUploadPlugin:54` (upload) | node exists | Results are uploaded every 15 s instead of every minute. | Once, when the timer is set up. |

`DefaultInit` and `PluginApplicationLauncher` also run for JTeams and, since the Veranstaltungswertung uses the plugin
manager, for the Veranstaltungswertung. Neither application has the beta plugin (`plugins-mmm/`,
`plugins-veranstaltung/`), but they share the `jauswertung` preferences, so a stored switch applies to them as well.

## State of the beta plugin

`OBetaPlugin` is wired up but does nothing yet:

- Its options panel is only shown in development mode (`getPanels()`, `:77`).
- Its two checkboxes, "EnableBetaFeatures" and "EnableOldFeatures", stay disabled until a `BetaListener` registers
  (`addBetaListener`/`addOldListener`), and nothing registers one. Nothing uses the "old features" switch, and no old
  feature is left that it could switch.
- The plugin only adds itself to the options dialog in `update()`, which runs when a listener registers. Since none does,
  the panel never appears, not even in development mode.
- The state lives in two fields, so it is lost on restart.
- Both checkboxes set their tooltip to the key instead of the text (`setToolTipText("EnableOldFeatures")`), and
  `setName` is called twice, so the tab would be titled with the tooltip text of `BetaOptions` instead of `Beta`.

## Constraints

- `FormelManager` and `Laufliste` are in `jauswertung-data`, which must not depend on a plugin; the beta state has to be
  readable from a low module.
- Five of the switches are read once at startup or in static initializers (`FormelManager`, `JNewHeatsWizard.DSM`,
  `BSwitchAgeGroupsPlugin.ENABLED`, `DefaultInit`, `PluginApplicationLauncher`), before the options dialog can be
  opened. They have to read the persisted state, and a change either takes effect after a restart or needs a listener.
- `AGENTS.md` (*Swing Threading*, `:90-92`) and the files in `development/` describe development mode for the EDT checks;
  they need to be updated with the new switch.

## Proposed design

1. **Persisted switches in `jauswertung-data`.** An enum (e.g. `de.df.jauswertung.util.BetaFeature`) with one constant
   per switch and a reader/writer on the Preferences node `jauswertung/beta` (`Utils.getPreferences().node("beta")`).
   It is the only place that knows the node; low modules read it directly, `OBetaPlugin` writes it.
2. **`OBetaPlugin` shows one checkbox per switch**, always, not only in development mode. It loads the state from the
   node on `cancel()`/startup and writes it on `apply()`. The `BetaListener` mechanism, the "old features" checkbox and
   `update()` go away. Because most switches are read at startup, the panel says that changes take effect after a
   restart; a listener is only worth it for switches that are cheap to make live (`Laufliste`, `JElektronischeZeit`).
3. **Replace each `isInDevelopmentMode*` call** with the matching `BetaFeature` read, then delete
   `isInDevelopmentMode`, `isInDevelopmentModeFor` and the files in `development/` (or turn them into scripts that set
   the new node, if command-line setup is still wanted for developer boxes).
4. **Migration:** none is needed for users. Developer boxes lose their switches once and set them again in the options.

### Decisions to make first

- **Should the diagnostics be beta options at all?** Users would then see developer switches such as "EDT checks" or
  "verbose plugin manager" in the options. Recommendation: put the five user features in the beta plugin and handle the
  diagnostics separately:
  - the bug-report logging and the About tabs need no switch (always log; always show the tabs, or move them to an
    "Information" button);
  - EDT checks and the verbose plugin manager become one "Developer diagnostics" checkbox in the same panel, read at
    startup;
  - the 15 s upload period becomes a beta option or is dropped.

  This narrows the *Done when* below: today it asks for every diagnostic to be switched by the beta plugin.
- **One switch per feature, or one "Enable beta features" switch for all?** One per feature matches today's keys and
  lets DSM be enabled without the hidden formulas. Recommendation: one per feature.

## Suggested steps

Each step can be its own commit:

1. `BetaFeature` with its preference node, and the rebuilt `OBetaPlugin` options panel.
2. Switch the user features over: formulas, DSM (both places), switch age groups, dummy timing source.
3. Switch or simplify the diagnostics according to the decision above, then update `AGENTS.md`.
4. Remove `isInDevelopmentMode*` and the old `development/` files.

## Done when

Every feature and diagnostic above is switched by the beta plugin, or no longer needs a switch, as decided above. The
state is persisted, the options panel is shown without development mode, and `isInDevelopmentMode`,
`isInDevelopmentModeFor` and the `development` preferences node are gone.
