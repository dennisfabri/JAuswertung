# Replace jgoodies-forms

`com.jgoodies:jgoodies-forms` is feature-complete but only receives sporadic releases. grep finds 193 Java files
using `com.jgoodies`, so this is the largest dependency to move.

It works and has no known problems, so this is optional. If done, migrate per module to MigLayout or plain `GridBagLayout`,
one module per commit.

## Done when

No module imports `com.jgoodies`, or the decision to keep it is recorded and the chore is deleted.
