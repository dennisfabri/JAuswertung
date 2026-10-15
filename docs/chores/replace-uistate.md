# Replace uistate

`org.lisasp.legacy:uistate` persists window and component state. It is a fork of a dead library, maintained by hand in
`uistate/`, and used by `jutils-legacy`.

## Findings (checked 2026-10-15)

- `UIStateUtils.initUIState()` (`jutils-legacy`) removes the tree, list, table, viewport and tabbed-pane handlers, so
  only window position, size and extended state and split-pane divider positions are stored, in the preferences node
  `jauswertung/uistate`. All 55 call sites go through `UIStateUtils.uistatemanage(...)`; only `UIStateUtils` imports
  `uistate`. The library has 757 lines.
- No maintained external library covers this (not searched in depth).

## Options

1. Inline an own window and split-pane state manager into `UIStateUtils` on top of `java.util.prefs` (about 100 lines).
   Keep the node name and the keys `x`, `y`, `width`, `height`, `extendedState` and `dividerLocation` so saved window
   positions keep loading; check how `UIStateManager` builds its key paths and pin that with a test. Recommended.
2. Move `uistate` unchanged into `jutils-legacy`. Removes the repository, keeps the unused handlers.
3. Keep as is.

## Decision

Keep for now (2026-10-15).

## Done when

No project depends on `uistate`; `build-dependencies.sh` and the workspace `AGENTS.md` no longer list it.
