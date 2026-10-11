#!/usr/bin/env bash
# Creates the compatibility fixtures of one year from the last commit before March 1 of that year.
#
#   ./generate-compatibility.sh <year> [ref]     ref defaults to main
#
# The commit is built in a temporary worktree; generator and CompatibilityTest run there, so .wk files and CSV
# snapshots both come from that commit. The result is copied to jauswertung-io/src/test/resources/compatibility/<year>
# for review and commit. Sibling dependencies in the versions that commit expects must be installed in ~/.m2.
set -euo pipefail

if [[ $# -lt 1 || ! $1 =~ ^[0-9]{4}$ ]]; then
    echo "Usage: $0 <year> [ref]" >&2
    exit 1
fi
YEAR=$1
REF=${2:-main}
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FIXTURES=jauswertung-io/src/test/resources/compatibility
TARGET="$ROOT/$FIXTURES/$YEAR"

if compgen -G "$TARGET/*.wk" > /dev/null; then
    echo "Already created, not overwriting: $TARGET" >&2
    exit 1
fi

COMMIT=$(git -C "$ROOT" rev-list -1 --before="$YEAR-03-01T00:00:00" "$REF")
if [[ -z $COMMIT ]]; then
    echo "No commit on $REF before $YEAR-03-01" >&2
    exit 1
fi
COMMIT_DATE=$(git -C "$ROOT" show -s --format=%cI "$COMMIT")
echo "=== $YEAR from $COMMIT ($COMMIT_DATE)"

WORK=$(mktemp -d)
WORKTREE="$WORK/jauswertung-$YEAR"
cleanup() {
    git -C "$ROOT" worktree remove --force "$WORKTREE" 2> /dev/null || true
    rm -rf "$WORK"
}
trap cleanup EXIT
git -C "$ROOT" worktree add --detach --quiet "$WORKTREE" "$COMMIT"

if [[ ! -f $WORKTREE/jauswertung-misc/src/main/java/de/df/jauswertung/misc/ErzeugeKompatibilitaetsWettkaempfe.java ]]; then
    echo "Commit $COMMIT predates the generator; past years need it copied in and adapted manually." >&2
    exit 1
fi

echo "=== Building"
(cd "$WORKTREE" && mvn -B -q -pl jauswertung-misc -am -DskipTests package dependency:build-classpath \
    -Dmdep.outputFile=classpath.txt)

echo "=== Generating competitions"
(cd "$WORKTREE/jauswertung-files/src/main/resources" && java -Djava.awt.headless=true \
    -cp "$WORKTREE/jauswertung-misc/target/classes:$(cat "$WORKTREE/jauswertung-misc/classpath.txt")" \
    de.df.jauswertung.misc.ErzeugeKompatibilitaetsWettkaempfe "$YEAR" "$WORKTREE/$FIXTURES/$YEAR")

echo "=== Creating CSV snapshots"
TEST=(mvn -B -q -pl jauswertung-io -am test -Dtest=CompatibilityTest -Dsurefire.failIfNoSpecifiedTests=false)
(cd "$WORKTREE" && "${TEST[@]}" > "$WORK/snapshots.log" 2>&1) || true
if ! (cd "$WORKTREE" && "${TEST[@]}" > "$WORK/verify.log" 2>&1); then
    cat "$WORK/verify.log" >&2
    echo "CompatibilityTest fails on $COMMIT after creating the snapshots" >&2
    exit 1
fi

mkdir -p "$TARGET"
cp -r "$WORKTREE/$FIXTURES/$YEAR/." "$TARGET/"
cat > "$TARGET/SOURCE.txt" << EOF
commit: $COMMIT
commit date: $COMMIT_DATE
generated: $(date -Iseconds)
EOF
echo "=== Created $FIXTURES/$YEAR - review and commit"
