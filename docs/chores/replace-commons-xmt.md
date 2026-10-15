# Replace commons.xmt

`com.pmease:commons.xmt` (versioned XML migration for the file formats) has a dead upstream and is maintained by hand in
`commons.xmt/`. grep finds six Java files in JAuswertung using it; it is pinned to a SNAPSHOT version in the root POM.

The file-format migration is core to loading old `.wk` files, so this needs a design decision: keep it as an own
project under a clear name and release it properly, or move the migration into JAuswertung. Old files must still load; the compatibility fixtures cover this.

## Done when

The dependency is either released as a stable version of an own library or removed, and no SNAPSHOT remains.
