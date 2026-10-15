# Check nrjavaserial

`com.neuronrobotics:nrjavaserial` is a fork of RXTX with few maintainers and slow releases. It is used for the serial
timekeeping connection; grep finds nine Java files in JAuswertung and AlphaServer referencing it.

Evaluate `com.fazecast:jSerialComm`, which is more actively developed and bundles its native libraries. Test with real
timekeeping hardware before switching.

## Done when

Either `jSerialComm` is in use, or the decision to keep `nrjavaserial` is recorded and the chore is deleted.
