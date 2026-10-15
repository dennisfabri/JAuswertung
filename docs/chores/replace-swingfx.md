# Replace swingfx

`net.java.swingfx:swingfx` is a legacy Swing effects library; the copy in the workspace (`swingfx/`) is maintained by
hand because the original is dead. grep finds three Java files using `net.java.swingfx`, and `jutils-legacy` depends on it.

Find out which effects are used and whether FlatLaf, the JDK or a maintained library covers them. Otherwise inline the used
classes into `jutils-legacy`.

## Done when

No project depends on `swingfx`; build order in `build-dependencies.sh` and the workspace `AGENTS.md` no longer list it.
