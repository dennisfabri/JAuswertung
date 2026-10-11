# Time limit check is always disabled

## Finding

`TimelimitsContainer.hasTimelimits()` always returns `false`. `TimeLimitsPlugin.dataUpdated` uses it to enable the
menu item that checks the time limits, so the check can never be started, even when time limits are defined.

## Done when

`hasTimelimits()` reports whether any time limits are defined, and the check menu item is enabled exactly then.
