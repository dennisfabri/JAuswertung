# Replace l2fprod-common

`org.lisasp.legacy:l2fprod-common-*` (totd, tasks, buttonbar, fontchooser) is a fork of the dead L2FProd Swing
components, maintained by hand in `l2fprod-common/`. grep finds 20 Java files using `com.l2fprod`.

Replace per module: tip of the day, task pane, button bar and font chooser each need an own replacement or a small
component in `jutils-legacy`.

## Done when

No project depends on `l2fprod-common`; `build-dependencies.sh` and the workspace `AGENTS.md` no longer list it.
