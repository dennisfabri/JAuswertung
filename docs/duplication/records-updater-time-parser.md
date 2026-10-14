# RecordsUpdater's copy of the time parser recurses forever

`jauswertung-misc/.../recupdater/RecordsUpdater.java:215-258` is a copy of `ImportUtils.getZeit`
(`jauswertung-io/.../ImportUtils.java:1855-1890`). The original hands text times to `ImportUtils.getTime(String, …)`;
the copy calls its own `getTime(Object, …)` at `:250`, so every non-numeric time cell such as "1:05,23" ends in a
`StackOverflowError`, which `catch (RuntimeException)` does not catch. The copy also dropped "ja"/"yes".

Both copies silently wrap times of one hour or more (`while (d >= 360000) d -= 360000`).

## Done when

`RecordsUpdater` calls `ImportUtils.getZeit`/`getTime` (misc already depends on io).
