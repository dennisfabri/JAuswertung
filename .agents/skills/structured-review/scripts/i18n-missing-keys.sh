#!/usr/bin/env bash
# Reports I18n keys used in Java sources that are missing from jauswertung.properties. I18n falls back to the key
# itself for a missing key, so these show up in the UI as raw keys instead of failing.
#
# Usage: i18n-missing-keys.sh <path>...   (repo-relative or absolute; defaults to the whole repository)
# Only literal first arguments of I18n.get / getToolTip / getKeyStroke are checked; calls with a computed key are
# counted and listed so a reviewer can inspect them by hand. Exit code 1 if a key is missing.

set -euo pipefail
export LC_ALL=C

repo_root=$(git rev-parse --show-toplevel)
properties="$repo_root/jauswertung-data/src/main/resources/jauswertung.properties"
cd "$repo_root"

if [ "$#" -eq 0 ]; then
  set -- .
fi

defined=$(mktemp)
used=$(mktemp)
trap 'rm -f "$defined" "$used"' EXIT

# Lines after one ending in an odd number of backslashes continue the previous value and hold no key.
tr -d '\r' < "$properties" | awk '
  !continued && !/^[[:space:]]*([#!]|$)/ {
    key = $0
    sub(/^[[:space:]]+/, "", key)
    sub(/[[:space:]]*[=:].*$/, "", key)
    sub(/[[:space:]].*$/, "", key)
    print key
  }
  { match($0, /\\+$/); continued = (RLENGTH > 0 && RLENGTH % 2 == 1) }
' | sort -u > "$defined"

grep -rnoP --include='*.java' 'I18n\.(get|getToolTip|getKeyStroke)\(\s*"(?:[^"\\]|\\.)*"(?=\s*[,)])' "$@" \
  | sed -E 's/^([^:]+:[0-9]+):I18n\.(get|getToolTip|getKeyStroke)\(\s*"(.*)"$/\1\t\2\t\3/' \
  | awk -F'\t' '{
      prefix = ($2 == "getToolTip") ? "ToolTip." : ($2 == "getKeyStroke") ? "KeyStroke." : ""
      print prefix $3 "\t" $1
    }' | sort > "$used"

missing=$(join -t $'\t' -v 1 "$used" "$defined" || true)

computed=$(grep -rnP --include='*.java' 'I18n\.(get|getToolTip|getKeyStroke)\(\s*(?!"(?:[^"\\]|\\.)*"\s*[,)])[^\s)]' "$@" \
  | grep -v 'jauswertung-data/src/main/java/de/df/jauswertung/gui/util/I18n.java' || true)

echo "Literal keys checked: $(cut -f1 "$used" | sort -u | wc -l)"
if [ -n "$missing" ]; then
  echo "Missing keys ($(echo "$missing" | cut -f1 | sort -u | wc -l)):"
  echo "$missing" | awk -F'\t' '{ printf "  %s  (%s)\n", $1, $2 }'
else
  echo "Missing keys: none"
fi
if [ -n "$computed" ]; then
  echo "Calls with a computed key, check by hand ($(echo "$computed" | wc -l)):"
  echo "$computed" | sed -E 's/^([^:]+:[0-9]+):\s*/  \1  /'
fi

[ -z "$missing" ]
