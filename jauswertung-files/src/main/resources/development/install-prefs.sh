#!/bin/bash

DEST="$HOME/.java/.userPrefs/jauswertung/development/prefs.xml"
SRC="$(dirname "$0")/prefs.xml"

mkdir -p "$(dirname "$DEST")"

if [ -f "$DEST" ]; then
    BACKUP="${DEST}.$(date +%Y%m%d_%H%M%S).bak"
    cp "$DEST" "$BACKUP"
    echo "Backup created: $BACKUP"
fi

cp "$SRC" "$DEST"
echo "Copied to: $DEST"
