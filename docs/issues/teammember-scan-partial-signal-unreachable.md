# Team member scan never shows the "Names" signal

After a team's barcode is scanned, `JTeammembersBarcodeScanPanel` (`jauswertung-plugins-teammembersscan`) chooses one of
three signals (`:243-249`):

```java
if (s.isMannschaftsmitgliederComplete()) {
    signalZWOk();
} else if (s.isMannschaftsmitgliederComplete()) {
    signalZWDNS();
} else {
    signalZWNok();
}
```

The `else if` repeats the first condition, so `signalZWDNS()` can never run and the orange "Namen" signal
(`statusPartial`, `:536`) never lights up. A team whose members are only partly complete is shown as red
"Unvollständig", the same as a team without any usable member data. The code is unchanged since the import into Git
(648645dd), so the intended condition is not documented; `Mannschaft.hasMannschaftsmitglieder()` (some members
entered) is the likely candidate.

## Done when

The intended condition for the orange signal is decided and used in the `else if`, or the signal and `signalZWDNS()`
are removed if a third state is not wanted.
