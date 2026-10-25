# AlphaTiming

AlphaTiming liest die Daten einer Quantum- oder Ares-Zeitnahme über TCP oder eine serielle Schnittstelle, speichert sie
in einer Projektdatei (`.timing`) und zeigt den laufenden Lauf an. Es wird mit JAuswertung installiert:

- **AlphaTiming** im Startmenü: die grafische Oberfläche.
- `alphatiming-tui` im Installationsverzeichnis: dieselben Funktionen als Text-Oberfläche für die Konsole.
- `alphatiming-cli` im Installationsverzeichnis: zeichnet in eine vorhandene Projektdatei auf, bis eine Taste gedrückt
  wird (`alphatiming-cli <Projektdatei>`).

## Zeiten in JAuswertung übernehmen

AlphaTiming stellt die Läufe während der Aufzeichnung im selben Format bereit wie AlphaServer. In JAuswertung unter
*Optionen* → *Elektronische Zeitnahme* die Quelle *AlphaServer / AlphaTiming* wählen und als Server den Rechner
eintragen, auf dem AlphaTiming läuft. Verwendet AlphaTiming nicht den Port 1999, wird er angehängt, z. B.
`192.168.1.20:2000`.
