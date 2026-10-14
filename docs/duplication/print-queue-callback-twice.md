# Print-queue status and error handling exists twice

`PPrintPlugin.java:300-384` and `PPrintTeammembersPlugin.java:110-195` both contain `PrintCallback`,
`StatusTextChanger` and `ErrorScheduler` (CPD 371 tokens). Both spin in
`while (!ok) ok = EDTUtils.executeOnEDTwithReturn(...)` (`PPrintPlugin.java:379`, `PPrintTeammembersPlugin.java:191`)
while the main window is disabled.

## Done when

One implementation lives in `jauswertung-print` (or jutils-print offers a default `IPrintCallback` for a status bar)
and the busy loop is replaced by a retry or listener.
