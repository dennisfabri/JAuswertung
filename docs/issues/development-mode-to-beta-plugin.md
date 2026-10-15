# Development mode features belong in the beta plugin

Development mode is the Java Preferences node `jauswertung/development` (`Utils.isInDevelopmentMode()`,
`isInDevelopmentModeFor(type)` in `jauswertung-data/.../util/Utils.java:128-147`). It can only be switched on from
outside the application (`jauswertung-files/src/main/resources/development/install-prefs.sh`, `.reg`, `prefs.xml`), so
the features behind it are invisible to users. All of them should be switched through `OBetaPlugin`
(`jauswertung-plugins-editor/.../gui/plugins/OBetaPlugin.java`, descriptor `de.dm.auswertung.beta.plugin`) instead.

## What development mode switches today

Features:

- `FormelManager:64` (data): non-public scoring formulas are offered.
- `Laufliste:817` (data) and `JNewHeatsWizard:60` (heats), `"DSM"`: DSM mode for heat generation.
- `BSwitchAgeGroupsPlugin:28` (input), `"SwitchAgeGroups"`: button to switch age groups.
- `JElektronischeZeit:154` (input): source address `dummy` generates test heats.

Diagnostics:

- `DefaultInit:53` (swing): `CheckThreadViolationRepaintManager` (EDT checks).
- `PluginApplicationLauncher:79` (plugins-core), `"PluginManager"`: verbose plugin manager.
- `BugreportPlugin:39,84` (bugreport): bug-report handling in development mode.
- `AboutDialogCreator:25` (info): system and UI information in the About dialog.
- `MUploadPlugin:54` (upload): upload every 15 s instead of every minute.

## State of the beta plugin

`OBetaPlugin` is wired up but does nothing yet: its options panel is only shown in development mode (`:77`), its two
checkboxes ("EnableBetaFeatures", "EnableOldFeatures") stay disabled until a `BetaListener` registers, nothing registers
one, and the state is kept in memory only, so it is lost on restart.

## Constraints

- `FormelManager` and `Laufliste` are in `jauswertung-data`, which must not depend on a plugin; the beta state has to be
  readable from a low module (e.g. a flag in `Utils` that the plugin sets, or the preference the plugin writes).
- Several checks run once at startup or in static initializers (`DefaultInit`, `PluginApplicationLauncher`,
  `BSwitchAgeGroupsPlugin.ENABLED`, `JNewHeatsWizard.DSM`), before the options can be changed; they need to read the
  persisted state or react to `BetaListener` changes.
- `AGENTS.md` (*Swing Threading*) and `install-prefs.sh` describe development mode for the EDT checks; they need to be
  updated with the new switch.

## Done when

Every feature and diagnostic above is switched by the beta plugin, its state is persisted, its options panel is shown
without development mode, and `isInDevelopmentMode`/`isInDevelopmentModeFor` and the `development` preferences node are
gone.
