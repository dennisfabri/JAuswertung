# JAuswertung

Die Benutzeroberfl�che von JAuswertung teilt sich in vier Bereiche:

- Die Men�s
- Die obere Knopfleiste
- Die linken Knopfleisten
- Die Eingabebereiche

�ber die linken Knopfleisten k�nnen Sie ausw�hlen, welche Eingaben Sie gerade durchf�hren wollen. Die rechts daneben liegenden Eingabebereiche werden dann entsprechend umgestellt. Die obere Knopfleiste (direkt �ber den linken Knopfleisten und den Eingabebereichen) dient dazu, h�ufig genutzte Funktionen der Men�s direkt zug�nglich zu machen. Alle weiteren Funktionen sind �ber die Men�s zu erreichen.

![JAuswertung](jauswertung.png)

## Die Men�s

![Men�s](menu.png)

JAuswertung bietet vier Men�s. Das Men� "Datei" befasst sich mit allen Informationen und Vorg�ngen zum Laden und Speichern von Dateien. Das Men� "Werkzeuge" enth�lt die Funktionen, die Sie bei der Verwaltung eines Wettkampfes ben�tigen. Das Men� Extras enth�lt weitere Funktionen, die nicht direkt mit der Auswertung von Wettk�mpfen zu tun haben. Das Men� "?" enth�lt weitere Informationen �ber JAuswertung.

### Das Men�: Datei

![Men�: Datei](menu-datei.png)

#### Neu, �ffnen, Speichern, Speichern unter...

![Neuen Wettkampf erstellen](neuer-wettkampf.png)

�ber den Men�punkt "Neu" k�nnen Sie einen neuen Wettkampf erstellen. Es �ffnet sich ein Fenster, wie es in Abbildung "Neuen Wettkampf erstellen" dargestellt ist. W�hlen Sie einfach die Art von Wettkampf, die Sie durchf�hren m�chten. Sie haben dabei die Wahl zwischen Einzel- und Mannschaftswettk�mpfen. Benutzerdefinierte Wettk�mpfe k�nnen ebenfalls erstellt werden, dazu k�nnen �ber die Kn�pfe "Benutzerdefiniert" mit dem Regelwerkseditor erstellte Dateien geladen werden. �ber die Men�punkte "�ffnen", "Speichern" und "Speichern unter ..." stehen die �blichen Funktionen zur Verf�gung.

#### Importieren, Exportieren, Automatisch speichern

Sie k�nnen verschiedene Daten importieren bzw. exportieren. Ein Assistent begleitet den Vorgang und bietet die zur Verf�gung stehenden M�glichkeiten an. Importiert werden k�nnen dabei Meldedaten, Mannschaftsmitglieder, Kampfrichter und Zusatzwertung-Punkte aus Excel- und CSV-Dateien. Exportiert werden k�nnen Meldedaten, Lauflisten, Zusatzwertung-Listen, Kampfrichter, Startkarten, Mannschaftsmitglieder, Zusatzwertung-Punkte und Ergebnisse in mindestens eins der Formate HMTL, CSV, XML, Excel oder PDF.

**Hinweis:** Sie k�nnen auch alle Informationen, die Sie aus JAuswertung drucken k�nnen, als PDF-Datei speichern. Nutzten Sie dazu im Druckbereich die Vorschaufunktion. W�hrend die Vorschau angezeigt wird, steht dort auch ein Knopf "Speichern als PDF" zur Verf�gung.

![Daten](importieren-daten.png)

**Details zum Import** Beim Import von Tabellen in JAuswertung m�ssen einige Bedingungen erf�llt sein. Verwenden Sie zum Import von Meldungen am besten die offiziellen Vorlagen, die Sie auf [dennisfabri.de](https://www.dennisfabri.de) finden.

**Was wird importiert?** Enth�lt eine Tabelle mehr als ein Tabellenblatt, so werden die Daten aller Bl�tter importiert. Tabellenbl�tter, die nicht den Anspr�chen gen�gen werden ignoriert.

**Wie kann ich Probleme beim Import vermeiden?** Tabellen sollten keine verbundenen Zellen und keine Formeln enthalten. Die erste Zeile einer Tabelle darf nicht leer sein.

**Wie muss eine importierbare Tabelle aufgebaut sein?** Die erste Zeile der Tabelle wird als �berschrift interpretiert. Dabei werden die folgenden Texte erkannt "S#" und "Startnummer" (f�r Startnummer), "Name" (bei Einzelwettk�mpfen "Vorname" und "Nachname"), "Gliederung", "Altersklasse", "Geschlecht", "Meldepunkte" und "Gemeldete Punkte", "Au�er Konkurrenz". Es wird erwartet, dass jede Zeile in der Tabelle genau einem Teilnehmer entspricht. Dabei wird davon ausgegangen, dass z.B. in der Spalte mit dem Titel "Name" die Namen der Mannschaften aufgef�hrt sind. Die Tabelle darf beliebig viele Spalten enthalten; Spalten deren Titel JAuswertung unbekannt ist, werden einfach ignoriert. Beachten Sie aber, dass die Felder "Name" (bzw. "Vorname" und "Nachname"), "Gliederung", "Altersklasse" und "Geschlecht" Pflichtfelder sind. Fehlt eines dieser Felder, kann JAuswertung die Tabelle nicht importieren.

**Hinweis:** Es werden nur Excel-Dateien ab der Versionen 97 unterst�tzt. Dateien �lterer Versionen m�ssen zuerst in einem neueren Format gespeichert werden.

**Hinweis:** Beachten Sie bitte ebenfalls, dass nach dem Regelwerk in der offenen Altersklasse eine Wahlm�glichkeit bei den Disziplinen besteht. In diesem Fall ist auch die Festlegung der Disziplinen beim Import Pflicht. Wie die Disziplinen festgelegt werden k�nnen, ist im n�chsten Abschnitt beschrieben.

**Welche zus�tzlichen Informationen k�nnen importiert werden?**
Dar�ber hinaus k�nnen f�r die jeweiligen Disziplinen Zeiten gemeldet werden, die bei der Lauflistenerstellung ber�cksichtigt werden k�nnen. Dazu m�ssen Sie nur f�r jede Disziplin eine Spalte anlegen, deren Titel der Name der Disziplin ist. Dort k�nnen Sie dann die Zeit im Formt "m:ss,00" (also Minuten:Sekunden,hundertstel) eintragen. Nach dem Regelwerk von 2007 darf die Offene Altersklasse ihre Disziplinen w�hlen. Diese Informationen k�nnen ebenfalls importiert werden. Ist in einer Spalte einer Disziplin keine Meldezeit angegeben, so wird der Teilnehmer hier nicht gemeldet. K�nnen Sie keine Zeit angeben gen�gt ein "+" zur Meldung des Schwimmers.

![Daten](exportieren-daten.png)

**Automatisch speichern** Die Option "Automatisch speichern" speichert die aktuelle Datei alle 5 Minuten. Dabei wird der aktuelle Name der Datei um "-autosave" erweitert. Hei�t ihr Wettkampf "BezMei06.wk" (".wk" ist die Endung, die von JAuswertung f�r Wettk�mpfe benutzt wird), wird alle 5 Minuten eine Datei unter dem Namen "BezMei06-autosave.wk" gespeichert. Wurde eine Datei neu erstellt und noch nicht gespeichert, ist automatisches Speichern nicht m�glich!

##### Drucken

Das Drucken ist in JAuswertung �ber eine der linken Knopfleisten verf�gbar. Dies ist notwendigt, da JAuswertung wesentlich mehr Druckm�glichkeiten bietet als die meisten anderen Programme. Da aber ein Men�punkt "Drucken" an dieser Stelle erwartet wird, ist dieser hier eingef�gt, und wechselt auf die entsprechende Druckansicht.

##### Eigenschaften

�ber diesen Men�punkt k�nnen verschiedene Angaben zum Wettkampf wie z.B. Name, Ort und Datum eingetragen werden, die f�r ein Protokoll ben�tigt werden. Einige Angaben wie z.B. der Name des Wettkampfs werden auch im Titel der Ergebnisse aufgef�hrt. Diese Angaben werden f�r das Protokoll ben�tigt.

Die Eingaben der Registerkarten "Wettkampf" und "Veranstaltungsort" enthalten strukturierte Informationen zum Wettkampf. Dar�ber hinaus werden die Felder "Name", "Ort" und "Datum" auf alle Seiten gedruckt. Die Registerkarte "Informationsseite" enth�lt ein Feld zur freien Texteingabe. Diese Informationen k�nnen �ber "Informationsseite" gedruckt werden. F�r das Deckblatt des Protokolls k�nnen Sie �ber die Registerkarte "Logo" ein Veranstaltungslogo ausw�hlen.

![Wettkampf](eigenschaften-wettkampf.png)
![Veranstaltungsort](eigenschaften-veranstaltungsort.png)
![Infoseite](eigenschaften-infoseite.png)
![Logo](eigenschaften-logo.png)

##### Zuletzt genutzte Dateien

In der Liste der zuletzt genutzten Dateien, werden die vier letzten ge�ffneten bzw. gespeicherten Dateien angezeigt. Durch einen Mausklick werden diese direkt geladen.

##### Beenden

�ber diesen Men�punkt wir JAuswertung beendet. Dies kann je nach Betriebssystem auch �ber das "X" in der rechten oberen Ecke des Hauptfensters geschehen. Wurden seit dem letzten Speichern Daten ge�ndert, erfolgt eine Sicherheitsabfrage.

### Das Men�: Bearbeiten

Das Men� "Bearbeiten" enth�lt die Funktionen, die Sie bei der Verwaltung ihres Wettkampfs unterst�tzen. Dazu geh�rt das Erzeugen und Verwalten von Lauf- und Zusatzwertung-Listen, der Einsatz von Kampfrichtern, die �nderung des Regelwerks, die Strafenliste, fehlende Eingaben und die Kampfrichterverwaltung.

![Men�: Bearbeiten](menu-bearbeiten.png)

#### R�ckg�ngig und Wiederholen

�ber den Punkt "R�ckg�ngig" k�nnen Sie jeweils die letzte �nderung zur�cknehmen. Durch mehrfaches Ausw�hlen dieses Punkts k�nnen Sie auch mehrere Schritte zur�ckgehen. Sollten Sie einmal einen Schritt zu weit zur�ck gegangen sein, k�nnen Sie diesen Schritt �ber den Men�punkt "Wiederholen" wieder herstellen. Es ist auch m�glich mehrere Schritte wiederherzustellen.

### Das Men�: Vorbereiten

Dieses Men� enth�lt Funktionen zur Vorbereitung eines Wettkampfs. Teilweise werden die Funktionen auch w�hrend eines Wettkampfs noch ben�tigt.

#### Meldezeiten

![Meldezeiten](meldezeiten.png)

Meldezeiten werden zur gerechteren Einteilung der L�ufe ben�tigt. Wenn Sie also eine Laufliste mit der Option "Nach Meldezeiten sortieren" erstellen wollen, sollten Sie zuerst hier die entsprechenden Meldezeiten eingeben.

Im oberen Bereich des Fensters k�nnen Sie die Altersklasse und das Geschlecht f�r die Eingabe w�hlen. Im Hauptbereich werden dann alle Schwimmer dieser Auswahl mit den Meldezeiten jeder Disziplin angezeigt. Sind f�r Schwimmer einzelne Diszplinen deaktiviert, so nimmt der Schwimmer an diesen Disziplinen nicht teil.

#### Meldepunkte sch�tzen

![Meldepunkte sch�tzen](meldepunkte-schaetzen.png)

�ber diesen Men�punkt k�nnen Sie Meldepunkte auf der Basis gemeldeter Zeiten sch�tzen lassen. Dabei wird allen Schwimmern die Punktzahl zugewiesen, die sie mit diesen Zeiten (ohne Zusatzwertung und Punkteabz�ge) erhalten w�rden.

**Hinweis:** Sind bereits Meldepunkte gesetzt, werden diese beibehalten.

#### Zulassung

Die Zulassung hat 2 Unterpunkte: Neu, Nicht qualifizierte entfernen. Mit dem ersten Punkt kann �ber einen Assistenten eine neue Zulassung durchgef�hrt werden. Schwimmer, die bereits als gesetzt markiert werden, werden dadurch nicht ver�ndert. Die restlichen Schwimmer werden auf der Basis der Einstellungen als Direktqualifiziert (z.B. f�r Landesmeister), Punkte (Nachr�cker nach Punkten) und nicht qualifiziert gesetzt.

Wird keine Zulassung durchgef�hrt, sind alle Schwimmer auf "offen" gesetzt. Die Zulassung kann auch f�r jeden Schwimmer einzeln gesetzt werden. Daf�r steht die Funktion "Bearbeiten" z.B. unter Meldungen zur Verf�gung.

#### Laufliste

�ber diesen Men�punkt k�nnen Lauflisten erstellt und verwaltet werden. Dazu stehen Ihnen die folgenden Funktionen zur Verf�gung:

Der Unterpunkt "Neu" erstellt automatisch eine neue Liste, in der alle Teilnehmer bzw. Teams ber�cksichtigt werden. Sie werden durch einen Assistenten durch die Erstellung der Liste begleitet und k�nnen die Einstellungen ihren W�nschen anpassen:

- Schritt 1: Allgemeine Einstellungen
  - Festlegung der Anzahl der Bahnen des Schwimmbeckens
  - Nummer des ersten Laufs
- Schritt 2: Art der Erstellung
  - Hier k�nnen Sie w�hlen, ob sie den Assistenten fortsetzen und eine Laufliste automatisch erstellen wollen oder ob Sie eine leere Laufliste erstellen wollen, in der Sie die L�ufe selbst einteilen.
- Schritt 3: Bahnenauswahl
  - Hier k�nnen Sie f�r jede Disziplin festlegen, welche Bahnen genutzt werden sollen. In einigen Schwimmb�dern kann es aus baulichen Gr�nden n�tig sein, z.B. bei der Disziplin "50m Retten" wegen ungleicher Beckentiefe auf eine Bahn zu verzichten.
- Schritt 4: Reihenfolge der Altersklassen und Disziplinen
  - In diesem Schritt k�nnen Sie festlegen welche Disziplinen und welche Altersklassen in welcher Reihenfolge geschwommen werden sollen. Die Standardreihenfolge soll allen Schwimmern einen m�glichst gro�en Abstand zwischen zwei Starts erm�glichen.
- Schritt 5: Sortierung der Schwimmer je Altersklasse
  - Dieser Schritt legt fest, wie die Schwimmer innerhalb einer Altersklasse auf die L�ufe verteilt werden. Die Punkte "Zuf�llig", "Nach Gliederungen sortieren" und "Gleiche Gliederung auf unterschiedliche L�ufe verteilen" bieten sich vor allem f�r Freundschaftswettk�mpfe an, w�hrend "Nach gemeldeten Punkten sortieren", "Nach gemeldeten Zeiten sortieren (je Disziplin)" und "Nach Regelwerk sortieren" besonders f�r Meisterschaften geeignet sind.
- Schritt 6: Meldepunkte
  - Hinweis: Dieser Schritt wird nur angezeigt, wenn in Schritt 5 "Nach gemeldeten Punkten sortieren" ausgew�hlt wurde.
  - In diesem Schritt kann festgelegt werden, nach welchen Meldepunkten die Einteilung erfolgen soll.
- Schritt 7: Optionen
  - Durch "Fast leere L�ufe am Ende von Altersklassen vermeiden" werden fast leere "langsamste" L�ufe durch Schwimmer aus dem "zweitlangsamsten" Lauf aufgef�llt. **Beispiel:** G�be es also z.B. einen Lauf mit 1 Schwimmer und der n�chst bessere Lauf w�rde 5 Schwimmer enthalten, so werden zwei L�ufe mit 3 Schwimmern erstellt.
  - �ber "Gemischte L�ufe" wird ggf. der letzte m�nnliche und weibliche Lauf zusammengelegt, sofern dadurch ein Lauf gespart werden kann.
  - "Wenn m�glich aufeinanderfolgende L�ufe zusammenfassen" �berpr�ft nach der eigentlichen Erstellung der Liste, ob L�ufe, die aufeinanderfolgen, zusammengefasst werden k�nnen. Dabei wird nicht ber�cksichtigt, ob die L�ufe die gleiche Disziplin oder Altersklasse enthalten.
  - "Bahnen rotieren" erm�glicht es eine faierere Laufliste zu erstellen, da in jedem Lauf eine andere Bahn genutzt wird.
  - "Normale Teilnehmer und Teilnehmer au�er Konkurrenz in unterschiedlichen L�ufen" spricht eigentlich f�r sich selbst und trennt die entsprechend markierten Teilnehmer. Diese Option hat keinen Einfluss auf Option 2. Dar�ber hinaus werden die Teilnehmer nicht strikt getrennt, sondern nur entsprechend sortiert. Ist der letzte Lauf der Teilnehmer, die nicht au�er Konkurrenz starten, nicht voll besetzt, wird er durch die ersten au�er Konkurrenz Startenden aufgef�llt.

**Hinweis 1:** Bei der Sortieroption "Nach Regelwerk sortieren" werden die Altersklassen nach der jeweiligen Vorgabe des eingegebenen Regelwerks durchgef�hrt. Diese k�nnen Sie im Regelwerkseditor �ndern.

![Schritt 1: Allgemeine Einstellungen](laufliste-neu-1.png)
![Schritt 2: Laufliste erstellen oder nur eine leere anlegen?](laufliste-neu-2.png)
![Schritt 3: Benutzbare Bahnen w�hlen](laufliste-neu-3.png)
![Schritt 4: Reihenfolge der Disziplinen w�hlen](laufliste-neu-4.png)
![Schritt 5: Sortierung festlegen](laufliste-neu-5.png)
![Schritt 6: Weitere Optionen](laufliste-neu-6.png)

�ber den Unterpunkt "Bearbeiten" wird die aktuelle Liste angezeigt. Wurden seit der Erstellung der Liste weitere Teilnehmer oder Mannschaften eingetragen, erscheinen diese im rechten Teil - der sogenannten "Warteliste". Den Hauptteil des Fensters nimmt die eigentliche Liste ein, die in Tabellenform dargestellt ist. Die erste Spalte ist mit "#" beschriftet und enth�lt die Laufnummer. Die Spalten "AK" und "Disziplin" enthalten den Namen der entsprechenden Altersklasse bzw. der geschwommenen Disziplin. Dabei gilt f�r die Altersklasse, dass hier der Name der Altersklasse inklusive Geschlecht angezeigt wird. Starten die Schwimmer einer Altersklasse in einem Lauf gemischt, so steht hier der Name der Altersklasse gefolgt von "gemischt". Starten in einem Lauf unterschiedliche Altersklassen steht hier nur "Gemischt". Entsprechend gilt dies f�r die Spalte "Disziplin", sobald unterschiedliche Disziplinen geschwommen werden, steht hier "Gemischt". Die restlichen Spalten stehen f�r die Nummer der Bahn und dort sind die entsprechenden Teilnehmer bzw. Mannschaften eingetragen.

![Laufliste](laufliste-bearbeiten.png)
![Laufliste: Rechtsklick](laufliste-bearbeiten-popup.png)

![Men�](laufliste-bearbeiten-menu.png)

Durch einen einfachen Klick auf einen Teilnehmer werden dessen Informationen am unteren Fensterrand dargestellt. Durch einen Doppelklick auf einen Schwimmer in der Liste wird er auf die Warteliste gesetzt und aus der Liste entfernt. Durch einen Doppelklick auf ein leeres Feld der Liste wird der ausgew�hlte Teilnehmer der Warteliste in die Liste eingetragen und aus der Warteliste entfernt. Sie k�nnen die Listen wahlweise auch mittels Drag'n'Drop ver�ndern. Ziehen Sie hierzu den Teilnehmer bzw. die Mannschaft einfach an die gew�nschte Stelle. Durch einen rechten Mausklick auf ein Feld in der Laufliste �ffnet sich ein Kontextmen�. Wenn Sie auf einen Schwimmer geklickt haben, k�nnen Sie diesen �ber den Punkt "Entfernen" aus der Laufliste entfernen. �ber die restlichen Men�punkte k�nnen Sie einen Lauf oberhalb bzw. unterhalb des aktuellen Laufs einf�gen oder den aktuellen Lauf l�schen.

�ber den Unterpunkt "Anzeigen" kann die aktuelle Lauf- bzw. Zusatzwertung-Liste angezeigt werden. Der Aufbau des Fensters entspricht dem bei der Bearbeitung; es fehlt lediglich die Warteliste und eine Bearbeitung ist nicht m�glich. So k�nnen Sie sicherstellen, dass Sie nicht versehentlich die Laufliste modifizieren.

�ber den Unterpunkt "Sperren" kann die Bearbeitung und die Neuerstellung unterbunden werden. Damit ist eine versehentliche �nderung der Listen nicht m�glich.

#### Zusatzwertungsliste

�ber diesen Men�punkt k�nnen Lauflisten erstellt und verwaltet werden. Dazu stehen Ihnen die folgenden Funktionen zur Verf�gung:

![Schritt 1: Anzahl der Zusatzwertung-Bahnen](hlw-liste-neu-1.png)
![Schritt 2: Erstellung oder Vorbereitung](hlw-liste-neu-2.png)
![Schritt 3: Reihenfolge der Altersklassen](hlw-liste-neu-3.png)
![Schritt 4: Sortierung der Teilnehmer](hlw-liste-neu-4.png)
![Schritt 5: Meldepunkte](hlw-liste-neu-5.png)
![Schritt 6: Uhrzeit](hlw-liste-neu-6.png)
![Schritt 7: Pause](hlw-liste-neu-7.png)
![Schritt 8: Optionen](hlw-liste-neu-8.png)

Die Erstellung einer Zusatzwertungsliste wird �ber "Neu" durchgef�hrt. Dabei geben Sie zuerst die Anzahl der Zusatzwertung-Puppen an. Im zweiten Schritt k�nnen Sie ausw�hlen, ob Sie die Zusatzwertung-Liste von Hand erstellen m�chten (Dann ist die Erstellung nach diesem Schritt abgeschlossen), oder ob Sie die Liste von JAuswertung erstellen lassen wollen. Im dritten Schritt legen Sie die Reihenfolge der Altersklassen fest. Der vierte Schritt gibt die Reihenfolge der Teilnehmer bzw. Mannschaften innerhalb einer Alterklasse an. Im f�nften Schritt legen Sie die Anfangsuhrzeit der Zusatzwertung sowie die Dauer einer Zusatzwertung fest. Im letzten Schritt k�nnen Sie noch Pause bei der Zusatzwertung einplanen. Diese k�nnen Sie wahlweise zu einem festen Zeitpunkt oder nach jeder Altersklasse angeben.

![Zusatzwertungsliste bearbeiten](hlw-liste-bearbeiten.png)
![Zusatzwertungsliste bearbeiten: Rechtsklick](hlw-liste-bearbeiten-popup.png)

Sie k�nnen bestehende Zusatzwertung-Listen bearbeiten, so dass Sie auch automatisch erstellte Listen Ihren W�nschen anpassen und nachgemeldete Starter einf�gen k�nnen. Dabei sehen Sie in dem Fenster links die aktuelle Zusatzwertung-Liste und rechts in der "Warteliste" die Starter, die noch zur Zusatzwertung eingeteilt werden m�ssen. Die Tabelle der Zusatzwertung-Liste enth�lt dabei die Spalten "Zeit" f�r den Zeitpunkt an dem die Zusatzwertung durchzuf�hren ist sowie die Altersklasse der Starter. Gehen gemischte Geschlechter zu einer Zeit an der Start steht hier z.B. "AK 13/14 gemischt". Starten unterschiedliche Altersklassen zum gleichen Zeitpunkte so steht in dieser Spalte "Gemischt". Die restlichen Spalten stehen f�r die Nummer der Bahn bzw. Zusatzwertung-Puppe und dort sind die entsprechenden Teilnehmer bzw. Mannschaften eingetragen. Eine Besonderheit stellt hier die Mannschafts-Zusatzwertung dar, da hier jede Mannschaft viermal antreten muss. Einzelne Teilnehmer bzw. Mannschaften k�nnen Sie per Drag'n'Drop verschieben und so an die gew�nschte Position bringen. Pausen und L�ufe k�nnen Sie �ber einen Rechtsklick-Men� hinzuf�gen bzw. entfernen.

Die Funktion "Anzeigen" bietet ausschlie�lich die Anzeige der Zusatzwertung-Liste ohne die Bearbeitungsfunktionen. �ber den Punkte "Sperren" k�nnen Sie die Zusatzwertung-Liste vor Bearbeitungen sch�tzen. So kann diese nicht versehentlich ge�ndert werden.

#### Regelwerkseditor

�ber diesen Men�punkt k�nnen Sie die Altersklassen ihres Wettkampfs bearbeiten. Beachten Sie aber, dass Sie w�hrend eines Wettkampfs keine Altersklassen erstellen bzw. l�schen k�nnen, und dass die Anzahl der Disziplinen nicht ge�ndert werden kann. Dar�ber hinaus k�nnen Sie weitere Einstellungen zum Regelwerk durchf�hren. Weitere Informationen zum Regelwerkseditor finden Sie im Kapitel [Regelwerkseditor](../regelwerkseditor/regelwerkseditor.html).

#### Grenzwerte

�ber diesen Men�punkt k�nnen Grenzwerte wie Rekorde oder Mindestzeiten gepflegt werden.

#### Kampfrichter

![Kampfrichtervorgabe w�hlen](kampfrichter-neu.png)
![Kampfrichterverwaltung](kampfrichter.png)
![Men�](kampfrichter-menu.png)

Mit der Kampfrichterverwaltung k�nnen Kampfrichter auf die verschiedenen Positionen eingeteilt werden. Die Positionen entsprechen denen des aktuellen DLRG-Regelwerks. Sie k�nnen je nach Wettkampf die Anforderungen an die Kampfrichter festlegen. So gelten z.B. f�r Bezirksmeisterschaften geringere Anforderungen als f�r die Landesmeisterschaften. Die Einteilung der Kampfrichter kann einzeln gedruckt werden, ist aber auch Bestandteil des Protokolls.

**Tipp:** Sie k�nnen die Einteilung der Kampfrichter auch speichern und in einem anderen Wettkampf laden. Damit m�ssen Sie die Kampfrichter nicht zweimal eintippen, wenn sich die Kampfrichter z.B. zwischen Einzel- und Mannschaftsmeisterschaften nicht �ndern.

#### Daten pr�fen

![Daten pr�fen: �bersicht](daten-pruefen.png)

�ber diesem Men�punkt �ffnen Sie ein Fenster, dass ihnen Informationen zum aktuellen Stand der Eingabe darstellt. Sie ershalten eine �bersicht �ber fehlerhafte bzw. korrekte Einfaben. Dabei m�ssen Sie jedoch beachten, dass diese Angaben auf Erfahrungswerten basieren und in vielen F�llen auf Fehler in der Eingabe hinweisen. Es wird damit nicht garantiert, dass bestimmte Eingaben mit absoluter Sicherheit richtig oder falsch sind. Es ist aber i.d.R. gut, sich markierte Punkte genauer anzusehen.

![Daten pr�fen: Anmeldung](daten-pruefen-anmeldung.png)
![Daten pr�fen: Zeiten](daten-pruefen-zeiten.png)
![Daten pr�fen: Zusatzwertung Punkte](daten-pruefen-hlw.png)
![Daten pr�fen: Mannschaftsmitglieder](daten-pruefen-mannschaftsmitglieder.png)

### Das Men�: Durchf�hren

#### Startunterlagenkontrolle

�ber diesen Men�punkt k�nnen Sie festlgen welche Schwimmer einer Startpasskontrolle unterzogen werden sollen. Dabei steht Ihnen ein Assistent zur Verf�gung, der Sie bei der Auslosung der Startunterlagenkontrolle unterst�tzt, so dass Sie die Auswahl nicht manuell vornehmen m�ssen. Diese Auswahl k�nnen Sie aber bei Bedarf jeder Zeit Ihren W�nschen anpassen.

![Startunterlagenkontrolle](startunterlagen-menu.png)
![Men�](startunterlagen-menu.png)
![Assistent](startunterlagen-assistent.png)

#### Dopingkontrolle

Die Auslosung der Dopingkontrolle ist identisch zur [Startunterlagenkontrolle](#Startunterlagenkontrolle).

#### Elektronische Zeitnahme

![Elektronische Zeitnahme](elektronische-zeitnahme.png)

Die Funktion "Elektronische Zeitnahme" wurde f�r die "Deutschen Meisterschaften im Rettungsswimmen" hinzugef�gt und erm�glicht die �bernahme von Zeiten aus einer elektronischen Zeitmessanlage. Wenn JAuswertung mit einer Zeitmessanlage verbunden ist, kann �ber den Knopf "Aktualisieren" das aktuelle Ergebnis abgerufen werden. Im Bereich �bersicht kann aus den L�ufen von JAuswertung ausgew�hlt werden. JAuswertung versucht dabei den passenden Lauf der elektronischen Zeitmessung im rechten Bereich zu ermitteln und anzuzeigen. Mit dem Knopf "Eintragen" werden die Zeiten aus der elektronischen Zeitmessung �bernommen.

Die Verbindung mit einer elektronischen Zeitnahme erfolgt �ber ein Netzwerk. Die ben�tigten Einstellungen stehen im Men�punkt Extras -> Optionen zur Verf�gung.

**Hinweis:** Ggf. stellt die Zeitnahme mehrere Zeiten pro Bahn zur Verf�gung. Aus diesen kann die gew�nschte Zeit ausgew�hlt werden.

#### Zielrichterentscheid

Der Zielrichterentscheid ist eine Besonderheit, die mit dem DLRG-Regelwerk von 2008 aus dem DSV-Regelwerk �bernommen wurde. Diese tritt dann ein, wenn in einem Lauf bei mehreren Schwimmer der gleichen Altersklasse die manuell gestoppte Zeit dem Zieleinlauf widerspricht (Bei vollautomatischer Zeitnahme tritt ein Zielrichterentscheid also nicht ein). In diesem Fall erhalten alle Schwimmer die gleiche Zeit. Die Platzierung in dieser Disziplin wird entsprechend dem Zieleinlauf festgelegt, so dass die Schwimmer trotz gleicher Zeit unterschiedliche Pl�tze einnehmen. Dies gilt jedoch nur, wenn nicht in einem anderen Lauf ein weiterer Schwimmer der gleichen Altersklasse die gleiche Zeit geschwommen hat.

![Zielrichterentscheid](zielrichterentscheid.png)

JAuswertung geht davon aus, dass die im Falle eine Zielrichterentscheids betroffenen Schwimmer die gleiche Zeit erhalten und entsprechend eingegeben werden. Wenn Sie also einen Zielrichterentscheid hinzuf�gen m�chten, listet Ihnen JAuswertung die in Frage kommenden L�ufe mit den entsprechenden Teilnehmern auf. Aus dieser Liste k�nnen Sie anschlie�end den richtigen ausw�hlen.

**Anmerkung:** Sollte Ihr Zielrichterentscheid nicht aufgef�hrt werden, so liegt wahrscheinlich ein Eingabefehler vor.

In der Liste der aufgef�hrten ausgew�hlten Zielrichterentscheide werden die Schwimmer zuerst anhand ihrer Bahnen sortiert aufgelistet. Sie k�nnen die Reihenfolge der Schwimmer per Drag'n'Drop �ndern.

![Zielrichterentscheid hinzuf�gen](zielirchterentscheid-add.png)
![Zielrichterentscheide korrigieren](Zielrichterentscheid-korrektur.png)

#### Urkunden

![Urkundeneditor](urkundeneditor.png)

Mit dem Urkundeneditor k�nnen Sie bestehende Urkunde bedrucken lassen, so dass Sie diese nicht von Hand ausf�llen m�ssen. Dazu k�nnen Sie einzelne Textfelder erstellen, ausrichten und deren Schriftart �ndern. �ber das normale Druckmen� k�nnen dann die Urkunden auch Altersklassenweise gedruckt werden.

Folgende Texte in den Textfeldern werden automatisch ersetzt:

- \<Name\>
- \<Gliederung\>
- \<Altersklasse\>
- \<Geschlecht\>
- \<Punkte\>
- \<Platz\>
- \<Wertung\>
- \<Mitglieder\>
- \<Mitglieder2\>

Beachten Sie bitte die \< und \> um jedes Wort. Der Satz "\<Name\> belegte in der Altersklasse \<Altersklasse\> \<Geschlecht\> den \<Platz\>. Platz" w�rde also z.B. auf der gedruckten Urkunde wie folgt aussehen "Musterstadt 1 belegte in der Altersklasse AK 12 weiblich den 3. Platz". Die Texte \<Mitglieder\> und \<Mitglieder2\> werden bei Mannschaftswettk�mpfen durch die Mannschaftsmitglieder ersetzt. \<Mitglieder\> erstellt dabei eine kommagetrennte Liste, \<Mitglieder2\> schreibt jedes Mitglied in eine eigene Zeile. Durch \<Wertung\> wird bei der Disziplinenwertung der Name der Disziplin eingesetzt; bei der Mehrkampfwertung wird hier "Mehrkampf" eingetragen.

#### Ausgabefilter

�ber diesen Men�punkt gelangen Sie in die Verwaltung der Ausgabefilter. Ausgabefilter erm�glichen es mehrere Wettk�mpfe in Einem zu verwalten und trotzdem getrennte Ergebnisse, Protokolle und Meldelisten drucken und exportieren zu k�nnen. Auf der linken Seite des Fensters sehen Sie die zur Zeit eingerichteten Filter. Hier k�nnen Sie einzelne Filter hinzugef�gen bzw. entfernen. Beachten Sie, dass der Standardfilter weder entfernt noch ge�ndert werden kann. Er enth�lt immer alle Teilnehmer bzw. Mannschaften. Auf der rechten Seite sehen Sie den zur Zeit ausgew�hlten Ausgabefilter. Hier k�nnen Sie den Namen des Filters �ndern, wobei leere Namen nicht erlaubt sind. Unter dem Namen finden Sie eine Liste der Gliederungen, die zu diesem Filter geh�ren. Mit den beiden darunterliegenden Kn�pfen k�nnen Sie weitere Gliederungen zu diesem Filter hinzuf�gen bzw. entfernen.

![Ausgabefilter](ausgabefilter.png)

### Das Men�: Information

#### Strafenkatalog

Der Strafenkatalog enth�lt eine Liste aller Strafen des Regelwerks, die in Kapitel und Paragraphen unterteilt sind. Werden w�hrend eines Wettkampfs Strafen vergeben und ein eigener Text eingetragen, werden diese "neuen" Strafen im Kapitel "Sonstiges" aufgef�hrt. Dies ist sinnvoll, da im Regelwerk z.B. Strafen f�r fehlerhafte Wechsel vorgesehen sind. W�hrend eines Wettkampfs ist aber auch der Vermerk "beim Wechsel vom 3. auf den 4. Schwimmer" sinnvoll. Wurde dies bei einer Strafe einmal eingetragen, kann diese beim n�chsten Auftreten direkt ausgew�hlt werden.

![Strafenkatalog](strafenkatalog.png)

Zur Unterst�tzung ist jeder Strafe ein Symbol zugeordnet:

- Gr�ner Haken: Keine Strafe (0 Strafpunkte)
- Blitz in gelbem Kreis: Strafpunkte
- Roter Kreis mit wei�em X: Disqualifikation
- Fu�spuren: Nicht angetreten
- Haus: Ausschluss

**Anmerkung:** Die Regelwerke f�r Mannschaftswettk�mpfe enthalten erweiterte Fehlercodes. Dabei wird neben der Strafe auch die Nummer des Verursachers bzw. des Wechsels angegeben. Z.B. kann f�r falsches Schleppen einer Puppe P2-3 gew�hlt werden, so dass direkt ablesbar ist, dass der dritte Schwimmer die Strafe P2 verursacht hat. Diese Erweiterung ist jedoch im Regelwerk so nicht vorgesehen.

#### Strafenliste

Die Strafenliste zeigt alle vergebenen Strafen des aktuellen Wettkampfs nach L�ufen sortiert an. Hier haben Sie die M�glichkeit, diese noch einmal auf Richtigkeit zu �berpr�fen und ggf. zu korrigieren.

![Strafenliste](strafenliste.png)

#### Statistiken

Unter diesem Men�punkt k�nnen einige Auswertungen rund um den Wettkampf angezeigt werden.

#### Laufpr�sentation

Die Laufpr�sentation dient zur Darstellung der Informationen �ber den aktuellen sowie dem n�chsten Lauf z.B. �ber einen Videobeamer. Die Anzeige wechselt zeitgesteuert zwischen der aktuellen Laufnummer und dem n�chsten Lauf.

Folgende Tasten k�nnen zum Wechsel zum n�chsten Lauf genutzt werden: Pfeil rechts, Pfeil unten, Leertaste, Enter

Folgende Tasten k�nnen zum Wechsel zum vorherigen Lauf genutzt werden: Pfeil links, Pfeil oben, Backspace.

#### Wettkampf herunterladen

Diese Funktion l�dt den Wettkampf von einer anderen laufenden Instanz von JAuswertung herunter. Dies ist nur in Kombination mit der Laufpr�sentation sinnvoll, um die Anzeige mit der aktuellen Eingabe abzugleichen.

### Das Men�: Extras

Das Men� "Extras" enth�lt die Men�punkte Strafenkatalog und Optionen.

![Men�: Extras](menu-extras.png)

#### Optionen

![ Optionen](optionen-allgemein.png )

�ber den Men�punkt "Optionen" k�nnen Sie ein Fenster �ffnen, in dem sich weitere Einstellungen vornehmen lassen. Das Fenster enth�lt die Registerkarten "Allgemein", "Anzeige", "HTTP-Server", "Sicherung", "CSV", "Drucken", "Druckschrift", "Elektronische Zeitnahme" und "Werbung".

�ber "Allgemein" k�nnen Sie einstellen, welche Art von Wettkampf also ein Einzel- bzw. Mannschaftswettkampf nach aktuellem Regelwerk beim Start erstellt werden soll. Die zweite Option legt fest, ob die Oberfl�che "geteilt" sein soll. Geteilt ist die Standardeinstellung und bedeutet, dass Sie die Ergebnisse jederzeit im unteren Bereich des Fensters sehen k�nnen. Sollten Sie jedoch eine niedrige Aufl�sung oder gr��ere Schriftarten nutzen, kann es sein, dass der Platz auf dem Bildschirm zu eng wird. Schalten Sie dann die geteilte Oberfl�che aus, und die Ergebnisse erreichen Sie ab dem n�chsten Start von JAuswertung �ber ein Icon an der linken Seite. Die dritte Einstellungsm�glichkeit betrifft das Drucken von Strafpunkten, wenn Sie diese Option aktivieren, wird automatisch bei jeder vergebenen Strafe ein Strafenlaufzettel mit allen n�tigen Daten gedruckt. Dies ist vor allem bei Freundschaftswettk�mpfen sinnvoll, wenn der Protokollf�hrer mehrere Funktionen im Kampfgericht erf�llt. Die letzte Option betrifft die Warnung bei der Eingabe von sehr hohen Zeiten. Da im Normalfall nur Zeiten geschwommen werden, die nicht h�her sind als 5 * Rec-Wert der aktuellen Disziplin, handelt es sich hier meistens um Tippfehler. Diese k�nnen so vermieden werden.

![Anzeige](optionen-anzeige.png)

Im Bereich "Anzeige" kann die geteilte Ansicht gew�hlt werden. Dabei wird das Fenster in einen oberen und einen unteren Bereich unterteilt. Der obere enth�lt die Eingabefunktionen und der untere die Punkte Ergebniss und Drucken. Dies ist jedoch nur bei hohen Aufl�sungen sinnvoll. Daher wird bei niedrigen Aufl�sungen die geteilte Oberfl�che nicht automatisch gew�hlt. F�r diese Einstellung muss JAuswertung neu gestartet werden. Der zweite Punkt beeinflusst die Ausrichtunge von Texten in Lauf- und Zusatzwertung-Listen. Mit dem dritten Punkt kann JAuswertung mitgeteilt werden, dass nach M�glichkeit redundante Informationen vermieden werden, um Platz auf den Ausdrucken zu sparen.

![Sicherung](optionen-sicherung.png)

�ber den Bereich "Sicherung" k�nnen Sie eine automatische Sicherung anlegen lassen. Diese unterscheidet sich von der Funktion "Datei" -> "Automatisch speichern" dadurch, dass den Dateinamen die aktuelle Uhrzeit hinzugef�gt wird. Dadurch existieren Backups mit denen die einzelnen �nderungen im Wettkampf nachvollzogen werden k�nnen.

![CSV](optionen-csv.png)

�ber die Registerkarte "CSV" k�nnen Einstellungen zum CSV-Export und -Import vorgenommen werden. Diese beziehen sich auf die Trennzeichen, die beim CSV-Export/-Import ber�cksichtigt werden. In normalen CSV-Dateien werden "," als Trennzeichen benutzt. Microsoft Excel nutzt aber die aktuellen Spracheinstellungen zum Export und Import. Dadurch werden Zahlen mit "," getrennt z.B. "123,4" und das Trennzeichen ";" genutzt. Je nachdem mit welchen Programmen Sie arbeiten, m�ssen Sie diese Option entsprechend w�hlen.

**Anmerkung:** Auf Windows-Systemen wird automatisch die zu Excel kompatible Version gew�hlt. Auf allen anderen Systemen die korrekte CSV-Darstellung.

![Elektronische Zeitnahme](optionen-elektronische-zeitnahme.png)

Die Verbindung von JAuswertung mit einer elektronischen Zeitmessanlage erfolgt �ber ein einfaches Netzwerkprotokoll. F�r die Zeitmessanlage "Ares 21" von "OMEGA Watches" steht eine Implementierung Namens "AlphaServer" zur Verf�gung. Als Server m�ssen Sie die Adresse des Rechners angeben, auf dem die Software l�uft.

**Hinweis:** Die Software "AlphaServer" ist ebenfalls OpenSource und damit kostenlos. Die grafische Benutzeroberfl�che ist jedoch noch nicht offiziell freigegeben. Genauere Informationen zu dieser Software k�nnen unter [info@dennisfabri.de](mailto:info@dennisfabri.de) nachgefragt werden.

![Werbung](optionen-werbung.png)

JAuswertung unterst�tzt das Einblenden von Werbebannern unter- bzw. oberhalb verschiedener Ausdrucke. Diese Einstellung wirkt sich nur auf Ausdrucke aus, die im Allgemeinen vor einem Wettkampf verschickt bzw. w�hrend eines Wettkampfs ausgeh�ngt werden. Ergebnisse k�nnen so z.B. den Logos von Sponsoren versehen werden.

![HTTP-Server](optionen-http-server.png)

�ber die Registerkarte "HTTP-Server" k�nnen Sie den integrierten HTTP-Server von JAuswertung einstellen.
Zur Zeit k�nnen Sie den Port f�r den HTTP-Server �ndern. Normalerweise sollten Sie den Standardport nutzen. Unter Linux kann es notwendig sein, den Port auf einen Wert �ber 1024 zu �ndern, da die unteren Ports f�r Benutzer gesperrt sind. Beachten Sie aber, dass sich damit der Aufruf in ihrem Browser �ndert. Der HTTP-Server kann normalerweise von ihrem Rechner aus �ber die URL "http://localhost/index.html" angesprochen werden. Wenn Sie den Port auf z.B. 8080 �ndern, sieht die URL wie folgt aus: "http://localhost:8080/index.html". Den Port f�r den HTTP-Server k�nnen Sie nur �ndern, wenn der HTTP-Server nicht gestartet ist.

**Hinweis:** Der eigentliche Start des HTTP-Servers erfolgt �ber die Weltkugel in der oberen Knopfleiste. Mit dem HTTP-Server k�nnen Sie, wenn Sie mehrere Rechner vernetzen, direkt auf die Daten von JAuswertung zugreifen, ohne den Auswerter st�ren zu m�ssen. Beachten Sie aber, dass jede Anfrage Rechenzeit ben�tigt und den Rechner des Auswerters damit zus�tzlich belastet. Dies gilt vor allem f�r den Download von PDF-Dateien. Sie sollten den HTTP-Server nur intern nutzen, da er nicht darauf ausgelegt ist, gro�e Mengen an Daten/Anfragen zu bew�ltigen.

![Druckschrift](optionen-druckschrift.png)

�ber die Registerkarte "Druckschrift" k�nnen Sie die Schriftart f�r Ausdrucke ausw�hlen. Standardm��ig wird die Schriftart "DLRG Univers 55 Roman" gew�hlt, sofern Sie vorhanden ist.

![Drucken](optionen-drucken.png)

�ber die Registerkarte "Drucken" k�nnen Sie die Orientierung der Ergebnisse im Protokoll einstellen. Standardm��ig werden die Ergebnisse um 90 Grad gedreht gedruckt. Wird also das Protokoll im Hochformat gedruckt, erscheinen die Ergebnisse im Querformat. Bei dieser Einstellung werden die Ergebnisse in den meisten F�llen am besten gedruckt. Wenn Sie die Ergebnisse im Hochformat erhalten wollen, so m�ssen Sie die Option "Ergebnisse waagrecht drucken" aktivieren.

Sie k�nnen festlegen, ob beim Druck von Lauflisten leeren Bahnen mitgedruckt werden ("Leere Bahnen drucken"). Zus�tzlich k�nnen Sie festlegen, ob beim Druck von Startkarten auch f�r in einem Lauf nicht genutzte Bahnen Startkarten gedruckt werden sollen.

Bei der Disziplinenwertung ist es Geschacksache, ob die Punkte mitgedruckt werden sollen oder nicht ("Punkte bei der Disziplinenwertung drucken"). Zur einfacheren Unterscheidung von Ergebnissen unterst�tzt JAuswertung Checksummen, mit denen festgestellt werden kann, ob zwei Ergebnisse unterschiedlich sind. Diese Checksummen k�nnen auf jedes Ergebnis gedruckt werden ("Checksummen in Ergebnissen drucken"). Sind die Checksummen unterschiedlich, so sind auch die Ergebnisse unterschiedlich. Wenn die Checksummen gleich sind, ist die Wahrscheinlichkeit sehr hoch, dass die Ergebnisse gleich sind. In seltenen F�llen kann es jedoch vorkommen, dass zwei Ergebnisse mit gleichen Checksummen unterschiedlich sind. Mit der Option "Nicht angetreten in der Strafenliste drucken" kann festgelegt werden, ob in Ausdrucken der Strafenliste auch "nicht angetretene" Schwimmer ber�cksichtigt werden. "Platzierungen in Ergebnissen drucken" erm�glicht es zu w�hlen, ob die Platzierungen in den einzelnen Disziplinen bei der Mehrkampfwertung mitgedruckt werden.

Mit der Option "Namen der Mannschaftsmitglieder in der Zusatzwertung-Liste drucken" besteht die M�glichkeit zu w�hlen, ob die Namen der ersten vier Mannschaftsmitglieder in die Zusatzwertung-Liste gedruckt werden sollen.

Die St�rke von Graut�nen ist stark vom eingesetzten Drucker und den Einstellungen im Treiber abh�ngig. Daher kann die Intensit�t des Grautons f�r die Zeilenmarkierung in Tabellen manuell festgelegt werden. Dies geschiet �ber den Regler unter "Helligkeit der hervorgehobenen Zeilen".

### Das Men�: ?

![Men�: ?](menu-info.png)

Dieses Men� bietet Informationen zu JAuswertung. Der Punkt "Hilfe" f�hrt zu dieser Anleitung, der Punkt "Tipps" zeigt ausgew�hlte Hilfen an und der Punkt "Info", stellt einige Informationen zu JAuswertung dar.

![Tipps des Tages](tipps.png)
![�ber JAuswertung](about.png)

## Die obere Knopfleiste

![Obere Knopfleiste](knopfleiste.png)

Diese Knopfleiste stellt Funktionen zur Verf�gung, die weitestgehend �ber Men�s zu erreichen sind. Die Funktionen entsprechen von Links nach Rechts den folgenden Men�punkten:

- [Datei -> Neu](#Neu.2C_.C3.96ffnen.2C_Speichern.2C_Speichern_unter.E2.80.A6)
- [Datei -> �ffnen](#Neu.2C_.C3.96ffnen.2C_Speichern.2C_Speichern_unter.E2.80.A6)
- [Datei -> Speichern](#Neu.2C_.C3.96ffnen.2C_Speichern.2C_Speichern_unter.E2.80.A6)
- [Bearbeiten -> R�ckg�ngig](#R.C3.BCckg.C3.A4ngig_und_Wiederholen)
- [Bearbeiten -> Wiederholen](#R.C3.BCckg.C3.A4ngig_und_Wiederholen)
- [Vorbereiten -> Laufliste -> Bearbeiten/Anzeigen](#Laufliste)
- [Vorbereiten -> Zusatzwertungsliste -> Bearbeiten/Anzeigen](#Zusatzwertungsliste)
- [Vorbereiten -> Daten pr�fen](#Daten_pr.C3.BCfen)
- HTTP-Server starten
- ISC-Upload starten
- Ausgabefilter einstellen
- Informationen �ber den aktuellen Stand der Eingabe

Die Kn�pfe zur Bearbeitung der Laufliste und der Zusatzwertung-Liste nehmen dabei eine Sonderstellung ein. Ist die Sperrung zur Bearbeitung einer Liste aktiviert, so wird der Knopf nicht deaktiviert sondern auf die Funktion "Anzeigen" umgeschaltet.

Mit dem Icon "Weltkugel" k�nnen Sie den HTTP-Server aktivieren bzw. wieder deaktivieren. Mit dem HTTP-Server k�nnen Sie, wenn Sie mehrere Rechner vernetzen, direkt auf die Daten von JAuswertung zugreifen, ohne den Auswerter st�ren zu m�ssen. Beachten Sie aber, dass jede Anfrage Rechenzeit ben�tigt und den Rechner des Auswerters damit zus�tzlich belastet. Dies gilt vor allem f�r den Download von PDF-Dateien. Sie sollten den HTTP-Server nur intern nutzen, da er nicht darauf ausgelegt ist, gro�e Mengen an Daten/Anfragen zu bew�ltigen. Beachten Sie bitte, dass der Aufruf der Seiten von der Einstellung des Ports abh�ngt (siehe [Extras -> Optionen](#Optionen)]).

Rechts neben diesen Kn�pfen ist die Auswahl von "Ausgabefiltern" m�glich. Diese beeinflussen nicht die Eingabe und Auswertung eines Wettkampfs, k�nnen aber die Ausgabe einschr�nken. So ist es m�glich die Meisterschaften z.B. mehrerer Bezirke in einem Wettkampf durchzuf�hren und getrennte Ergebnisse zu erstellen. Die Ausgabefilter beziehen sich nur auf die Ausgabe von Ergebnissen, Protokollen und Meldelisten beim Drucken und beim Export. Ist das Auswahlfeld deaktiviert, sind keine Ausgabefilter definiert. Sie k�nnen �ber den Men�punkt [Durchf�hren -> Ausgabefilter](#Ausgabefilter) Ausgabefilter einrichten und �ndern. Funktione f�r Ausgaben z.B. Ausdrucke signalisieren, dass Sie von einem Ausgabefilter beeinflusst werden durch ein Icon "Auge".

## Linke Knopfleisten

�ber die linke Knopfleisten k�nnen Sie durch die einzelnen Hauptfunktionen von JAuswertung bl�ttern. Je nach ausgew�hlten Knopf wird die entsprechende Funktion aktiviert. Beachten Sie, dass einige Funktionen nicht immer aktiviert sind. Wenn die gesamte Funktionalit�t hinter einem Knopf deaktiviert ist, erscheint eine kurze Information dar�ber, wann diese Funktionalit�t genutzt werden kann.

![JAuswertung - Schwimmereingabe](panel-anmeldung.png)

### Eingabe

Durch einen Klick auf "Eingabe" wird die Eingabe von Teilnehmern oder Mannschaften aktiviert. Je nach aktuellem Wettkampf unterscheidet sich das Aussehen des Fensters, da bei einem Mannschaftswettkampf kein Vor- und Nachname existiert. Gelbe Felder m�ssen ausgef�llt werden, wei�e Felder k�nnen freigelassen werden. Wird keine Startnummer eingetragen, so wird automatisch die n�chste freie Startnummer f�r den Schwimmer gew�hlt.

Bei Mannschaftswettk�mpfen k�nnen Sie auch mehrere Mannschaften einer Gliederung auf einmal hinzuf�gen. Geben Sie dazu wie gewohnt die Daten in den oberen Feldern ein. Zus�tzlich schreiben Sie dann in das Feld Anzahl die gew�nschte Menge an Mannschaften und klicken auf den Hinzuf�gen-Knopf darunter. Die eingef�gten Mannschaften werden automatisch durchnummeriert. Wenn Sie also als Name "Musterort" gew�hlt haben, werden die Mannschaften "Musterort 1", "Musterort 2", ... genannt. Beachten Sie dabei aber, dass die Mannschaften bis auf den Namen identisch sind. Sie sind also alle in der gleichen Altersklasse und nehmen mit den gleichen Meldezeiten an den gleichen Disziplinen teil.

**Hinweis**: Wenn Sie neue Teilnehmer oder Mannschaften zu einem Wettkampf hinzuf�gen, nachdem Sie eine Laufliste erstellt haben, werden diese nicht automatisch in die Laufliste einsortiert. Sie k�nnen diese aber �ber den Lauflisteneditor manuell einsortieren, oder eine neue Laufliste erstellen.

### Meldungen

Hier wird die aktuelle Meldeliste dargestellt. �ber die Felder im linken Teil kann die Auswahl der dargestellten Teilnehmer bzw. Mannschaften eingeschr�nkt werden. Dabei sind die Textfelder Filter, dass hei�t gibt man z.B. nur den Buchstaben g ein, werden alle Teilnehmer bzw. Mannschaften angezeigt, die den Buchstaben g im Namen tragen. Sind alle Felder leer, werden alle Teilnehmer bzw. Mannschaften dargestellt. Durch einen Rechtsklick auf einen Teilnehmer bzw. eine Mannschaft wird ein Kontextmen� ge�ffnet, dass das Bearbeiten und L�schen erlaubt, es k�nnen hier aber auch Strafen vergeben werden.

![Meldungen](panel-meldungen.png)
![Meldungen: Rechtsklick](panel-meldungen-popup.png)
![Meldungen: Laufanzeige](panel-meldungen-laufanzeigen.png)
![Meldungen: Teilnehmer/Mannschaft bearbeiten](schwimmer-bearbeiten.png)
![Meldungen](panel-meldungen.png)

Die Fenster zur Bearbeitung von Teilnehmern und Mannschaften unterscheiden sich nur geringf�gig und sind im Wesentlichen selbserkl�rend.

Die Men�punkte "Disqualifizieren", "Ausschluss" und "Nicht Angetreten" f�gen dem ausgew�hlten Schwimmer bzw. der ausgew�hlten Mannschaft f�r alle Disziplinen diese Strafe hinzu. �ber den Men�punkt "Strafe vergeben" k�nnen Sie �ber einen Assistenten eine genauere Strafe vergeben.

### Zeiteneingabe

Hier k�nnen Sie Zeiten einzelner Schwimmer eingeben. Dazu k�nnen Sie im Bereich "Auswahl" angeben, welche Disziplin eingetragen werden soll und wie viele Schwimmer gleichzeitig angezeigt werden k�nnen. �ber den Knopf "weiter" werden alle Felder wieder freigegeben. Diese Knopf ver�ndert keine Daten, er schaft lediglich Platz f�r neue Eingaben. Im Bereich "Status" werden ggf. Eingabefehler angezeigt, dies soll nur zu ihrer Orientierung dienen.

![Zeiteneingabe mit fehlerhaften Angaben](panel-zeiteneingabe.png)

Im Bereich "Zeiteneingabe" findet die eigentlich Eingabe statt. Sie k�nnen in einem der Felder unter "Startnummer" die Startnummer eines Schwimmers eingeben. Genauere Angaben zu dem Schwimmer erscheinen dann direkt neben diesem Feld und das Feld unter "Zeiteneingabe" wird aktiviert und Sie k�nnen direkt eine Zeit eintragen. Beachten Sie, dass die Eingabe der Zeit direkt durchgef�hrt wird. Wenn Sie also eine Zeit von 1:23,4 eingeben, wird der entsprechende Schwimmer kurzzeitig eine Zeit von 0:00,1 dann von 0:01,2 dann von 0:12,3 und anschlie�end die richtige Zeit haben.

Es stehen zur einfacheren Eingabe folgende Tastaturk�rzel in den Feldern zur Zeiteneingabe zur Verf�gung:

- "c" �ffnet den Dialog, um eine Strafe nach Fehlercode zu vergeben. Diese Strafe wird zu bestehenden Strafen hinzugef�gt.
- "d" f�gt dem Teilnehmer/der Mannschaft eine "Disqualifikation" hinzu
- "n" setzt den Teilnehmer/die Mannschaft auf "Nicht angetreten" (allerdings nur wenn keine Zeit eingegeben wurde)
- "p" �ffnet ein Fenster zur Eingabe von Strafpunkten. Diese werden zu bestehenden Strafpunkten addiert.
- "#" entfernt eingegebene Strafen

![Laufzeiten](panel-laufzeiten.png)

![Strafenvergabe mittels Code](strafencode.png)

![Strafeneditor](strafeneditor.png)
![Strafeneditor mit nicht gew�hlter Disziplin (z.B. in derAK Offen)](strafeneditor-disziplin-nicht-gewaehlt.png)

![Strafenassistent: Code](strafenwizard-code.png)
![Strafenassistent: Details](strafenwizard-details.png)

![Strafpunkte vergeben](strafpunkte.png)

### Laufzeiten

Die Funktionen der "Laufzeiten" stehen nur zur Verf�gung wenn auch die Laufliste mit JAuswerung verwaltet wird, erm�glicht aber die h�chstm�gliche Eingabegeschwindigkeit. �ber den Punkt "Lauf" im Bereich "�bersicht" k�nnen Sie direkt den Lauf ausw�hlen, den Sie eingeben m�chten. Im Rest des Bereichs "�bersicht" werden n�here Informationen zu dem entsprechenden Lauf dargestellt. Im Bereich "Lauf" werden die einzelnen Teilnehmer in der Reihenfolge ihrer Bahnen aufgelistet und Sie k�nnen direkt die Zeiten in dieser Reihenfolge eingeben. Die Eingabe der Zeit wird direkt durchgef�hrt und das Ergebnis aktualisiert.

Es stehen zur einfacheren Eingabe folgende Tastaturk�rzel in den Feldern zur Zeiteneingabe zur Verf�gung:

- "c" �ffnet den Dialog, um eine Strafe nach Fehlercode zu vergeben. Diese Strafe wird zu bestehenden Strafen hinzugef�gt.
- "d" f�gt dem Teilnehmer/der Mannschaft eine "Disqualifikation" hinzu
- "n" setzt den Teilnehmer/die Mannschaft auf "Nicht angetreten" (allerdings nur wenn keine Zeit eingegeben wurde)
- "p" �ffnet ein Fenster zur Eingabe von Strafpunkten. Diese werden zu bestehenden Strafpunkten addiert.
- "#" entfernt eingegebene Strafen
- "z" oder "," zeigen den Zieleinlauf des aktuellen Laufs an

Der Knopf "Zielrichterentscheid" erm�glicht den Zugriff auf eine Funktion, die durch das DLRG-Regelwerk von 2008 eingef�hrt wurde. Die Beschreibung dazu finden Sie unter [Durchf�hren -> Zielrichterentscheid](#Zielrichterentscheid)

**Hinweis 1:** Mit den Cursor-Tasten k�nnen Sie zwischen den Bahnen und L�ufen wechseln.

**Hinweis 2:** Mit der Taste \<Enter\> k�nnen Sie die Eingabe einer Zeit beenden und zur n�chsten Bahn wechseln. Wenn Sie die letzte Zeit eingegeben haben, wird automatisch der Zieleinlauf angezeigt.

### Zusatzwertung Eingabe

Die Eingabe der Zusatzwertung-Punkte funktioniert nach dem gleichen System wie die Eingabe der Zeiten mit der "Zeiteneingabe". Als Besonderheit ist jedoch anzumerken, dass obwohl ein Teilnehmer bzw. eine Mannschaft gefunden wurde, dass Feld "Zusatzwertung-Punkte" deaktiviert bleiben kann. Ist dies der Fall ist der Schwimmer in einer Altersklasse, in der keine Zusatzwertung durchgef�hrt wird. Dieses Problem wird ebenfalls im Bereich "Status" angezeigt.

Bei der Eingabe von Mannschaften k�nnen die Punkte f�r jeden Schwimmer einzeln eingegeben werden. Dazu muss an die Startnummer ein Buchstabe "a", "b", "c" oder "d" angeh�ngt werden. Der entsprechende Buchstabe wird automatisch auf die Zusatzwertung-Startkarten gedruckt. Die ersten vier Schwimmer eine Mannschaft sind f�r die Zusatzwertung vorgesehen. Der f�nfte Schwimmer nimmt nicht an der Zusatzwertung teil.

![Eingabe von Zusatzwertung-Punkten](panel-hlw-eingabe.png)

**Hinweis:** Bei der Zusatzwertung-Eingabe erm�glichen es die Texte "n" und "n.a." einen Schwimmer als "Nicht angetreten" zu markieren. Mit "d" kann ein Schwimmer als disqualifiziert markiert werden.

### Zusatzwertung Barcodes

JAuswertung druckt auf ausge�llte Zusatzwertung-Checklisten und -Startkarten zwei Barcodes. Ein Barcode steht f�r "Bestanden" und der andere f�r "Nicht bestanden". Die Barcodes k�nnen mit handels�blichen Barcode-Scanners erkannt werden. Wird ein Barcode f�r einen Teilnehmer eingescannt, der bestanden hat, leuchtet "200 Punkte" in gr�n auf. Hat der Teilnehmer nicht bestanden, leuchtet "0 Punkte" in rot auf. Ist der Barcode nicht korrekt, leuchtet "Barcode nicht korrekt" in orange auf. Zus�tzlich werden in den ersten beiden F�llen weitere Informationen zu dem erkannten Teilnehmer angezeigt.

**Hinweis:** Nicht alle Barcode-Scanner simulieren eine Tastatur.

**Hinweis:** Geeignete Barcode-Scanner (1D-Code-Scanner) simulieren eine Tastatur. Achten Sie deshalb darauf, dass der Fokus auf das Eingabefeld gesetzt ist, wenn Sie einen Barcode scannen.

**Hinweis:** Sie k�nnen diese Funktion auch ohne Barcode-Scanner nutzen. Tippen Sie dazu einfach die Ziffern unter dem Barcode in das Feld "Eingabe" und best�tigen Sie mit \<Enter\>. Die Barcodes enthalten eine Checksumme und sind daher gegen Vertipper besser als die normale Zusatzwertung-Eingabe abgesichert.

![ Zusatzwertung-Barcodes](panel-hlw-barcodes.png )

### Mannschaftsmitglieder

In diesem Bereich k�nnen mit JTeams ausgedruckte Mannschaftsmeldeb�gen zur Meldung von Mannschaftsmitgliedern eingescannt werden. Der dort abgebildete QR-Code enth�lt alle Informationen, um die eingegebenen Mannschaftsmitlieder der richtien Mannschafts zuzuordnen.

**Hinweis:** Nicht alle 2D-Code-Scanner simulieren eine Tastatur.

**Hinweis:** Geeignete 2D-Code-Scanner simulieren eine Tastatur. Achten Sie deshalb darauf, dass der Fokus auf das Eingabefeld gesetzt ist, wenn Sie einen QR-Code scannen.

**Hinweis:** Es gibt neben 2D-Code-Scannern auch die M�glichkeit mittels App und Smartphone den Code einzuscannen und per Netzwerk zu �bertragen. Daf�r muss auf dem genutzen Rechner eine passende Zusatzsoftware des jeweiligen Anbieters installiert werden.

![ Mannschaftsmitglieder](panel-mannschaftsmitglieder.png )

### Ergebnisse

Die aktuellen Ergebnisse werden im unteren Bereich des Fensters dargestellt. Wenn Sie mehr Platz f�r andere Teile von JAuswertung ben�tigen, k�nnen Sie �ber die Optionen die "geteilte Darstellung" deaktivieren, wodurch die Ergebnisse �ber einen zus�tzlichen Knopf in der linken Knopfleiste erreichbar sind und nicht mehr dauerhaft den unteren Fensterbereich belegen.

![Ergebnisse](panel-ergebnisse.png)
![Ergebnisse: Rechtsklick](panel-ergebnisse-popup.png)

Durch die Kn�pfe im oberen Bereich kann die gew�nschte Altersklasse sowie das Geschlecht ausgew�hlt werden. Die Ansicht der Ergebniss kann auch zur Bearbeitung einzelner Schwimmer bzw. Mannschaften genutzt werden. �ber einen Rechtsklick �ffnet sich das Popup-Men�, das bereits unter "Meldeliste" beschrieben wurde. Zus�tzlich k�nnen bestimmte Funktionen per Doppelklick durchgef�hrt werden. Diese h�ngen von der Spalte ab, in die geklickt wurde:

- Platz, Name, Gliederung, Jahrgang, Punkte, Diff: �ffnet das Fenster zum Bearbeiten eines Schwimmers bzw. einer Mannschaft.
- Zeit, Punkte (Je Disziplin): �ffnet ein Fenster zur Eingabe der Zeit f�r diese Diziplin.
- Strafe (Je Disziplin): �ffnet den Assistenten zur Vergabe einer Strafe f�r diese Disziplin.
- Zusatzwertung: �ffnet ein Fenster zur Eingabe der Zusatzwertung-Punkte.

**Hinweis:** Bei der Zusatzwertung-Eingabe erm�glichen es die Texte "n" und "n.a." einen Schwimmer als "Nicht angetreten" zu markieren.

### Drucken

Hier k�nnen die Daten in verschiedenen Varianten gedruckt werden. Die Kn�pfe sind nur dann aktiviert, wenn die entsprechenden Daten gedruckt werden k�nnen. Zu jeder Druckfunktion existiert eine Vorschau. �ber diese kann auch das Seitenlayout eingestellt werden. Diese Einstellungen werden gespeichert und sind beim n�chsten Start von JAuswertung wieder verf�gbar.

Um die Druckfunktionen �bersichtlicher zu gestalten wurden diese gruppiert. Die Gruppen haben die �berschrift "Wettkampf", "Protokoll", "Ergebnisse", "Laufliste", "Zusatzwertung-Liste" und "Vordrucke". Jede einzelne Gruppe k�nnen Sie durch eine Klick auf die Titelzeile ein- bzw. ausblenden, so dass Sie nur die Funktionen sehen, die Sie zur Zeit ben�tigen. Beim Start von JAuswertung ist nur die Gruppe "Wettkampf" ausgeklappt.

![Drucken](panel-drucken.png)

- Wettkampf:
  - Meldungen: Ausdruck einer kompakten Liste der Meldungen
  - Meldezeiten: Ausdruck aller Meldungen mit Meldezeiten
  - Startunterlagenkontrolle: Ausdruck der zur Kontrolle ausgelosten Teilnehmer bzw. Mannschaften
  - Dopingkontrolle: Ausdruck der zur Kontrolle ausgelosten Teilnehmer bzw. Mannschaften
  - Regelwerkseinstellungen: Detailierter Ausdruck aller Einstellung des Regelwerkseditors
  - Disziplinen: Kompakte Liste aller Disziplinen nach Altersklassen
  - Statistiken: Statistiken zum Wettkampf wie Teilnehmerzahlen und teilnehmende Gliederungen sowie gliederungssezifische Statistiken
  - Strafenkatalog: Liste aller Strafen des Strafenkatalogs

![Drucken: Wettkampf](panel-drucken-wettkampf.png)

- Protokoll:
  - Protokoll: Vollst�ndiges Protokoll des Wettkampfs gem�� Regelwerk
  - Veranstaltungsinformationen: Ausdruck der Informationen aus "Datei" -> "Eigenschaften"
  - Kampfrichter: Liste der Kampfrichter
  - Informationsseite: Ausdruck der Informationsseite aus "Datei" -> "Eigenschaften"
  - Zielrichterentscheide: Liste der Zielrichterentscheide

![Drucken: Protokoll](panel-drucken-protokoll.png)

- Ergebnisse:
  - Ergebnisse: Liste der Ergebnisse
  - Disziplinenwertung: Ergebnisse der Disziplinenwertung
  - Disziplinenwertung (altersklassen�bergreifend): Ergebnisse der Disziplinenwertung, wobei nur nach Disziplinen nicht aber nach Alterklassen unterschieden wird.
  - Gesamtwertung: Gesamtwertung des Wettkampfs
  - Siegerliste: Liste der ersten drei Pl�tze f�r jede Altersklasse
  - Siegerliste je Disziplin: Liste der ersten drei Pl�tze f�r jede Disziplin.
  - Medaillenspiegel: Ausdruck eines Medaillenspiegels
  - Laufzeiten: Tabellarischer Ausdruck der Zeiten nach L�ufen
  - Strafenliste: Liste der vergebenen Strafen
  - Urkunden: Ausgef�llte Urkunden
  - Urkunden der Disziplinenwertung: Ausgef�llte Urkunden f�r die Disziplinenwertung
  - Weitermeldung: Ausdruck eines Meldebogens mit den Ergebnissen des Wettkampfs
  - Zusatzwertung-Ergebnisse: Ausdruck der Ergebnisse der Zusatzwertung
  - Schnellste Zeiten: Schnellste Zeiten je Disziplin und Altersklasse
  - Gebrochene Rec-Werte: Liste der gebrochenen Rec-Werte

![Drucken: Ergebnisse](panel-drucken-ergebnisse.png)

- Laufliste:
  - Laufliste: Tabellarische Laufliste
  - Sprecherliste: Detaillierte Laufliste f�r Veranstaltungssprecher
  - Laufliste f�r Kampfrichter: Kompakte und �bersichtliche Laufliste f�r Kampfrichter (Wenn die Schriftart nicht zu breit ist, werden zwei L�ufe in einer Zeile ausgedruckt)
  - Lauf�bersicht: Kurze �bersicht �ber den Verlauf des Wettkampfs
  - Laufeinteilung: Liste der Teilnehmer bzw. Mannschaften mit den jeweiligen Starts
  - Kompakte Laufeinteilung: Kompakte Version der Laufeinteilung (ob diese gedruckt werden kann, h�ngt von der Laufliste ab)
  - Bahnenliste: Liste der Starts je Bahn f�r Zeitnehmer
  - Ausgef�llte Startkarten: Ausgef�llte Startkarten (Weitere Informationen zum Umgang mit dem Ausdruck finden sich im Dokument "Startkartendruck")
  - Ausgef�llte Zieleinlaufkarten: Zieleinlaufkarten mit ausgef�lltem Lauf und Disziplin (Weitere Informationen zum Umgang mit dem Ausdruck finden sich im Dokument "Startkartendruck")

![Drucken: Laufliste](panel-drucken-laufliste.png)

- Zusatzwertung-Liste
  - Zusatzwertung-Liste: Tabellarische Zusatzwertung-Laufliste
  - Puppenliste: Liste der Teilnehmer je Bahn f�r Zusatzwertung-Kampfrichter
  - Ausgef�llte Zusatzwertung-Startkarten: Ausgef�llte Zusatzwertung-Startkarten f�r Kampfrichter (Weitere Informationen zum Umgang mit dem Ausdruck finden sich im Dokument "Startkartendruck")
  - Ausgef�llte Zusatzwertung-Checklisten: Ausgef�llte Zusatzwertung-Checklisten f�r Kampfrichter
  - Ausgef�llte Zusatzwertung-Startkarten (ohne Zeiten und Bahnen): Ausgef�llte Zusatzwertung-Startkarten - die Fehler Zeit und Bahn sind jedoch leer (Weitere Informationen zum Umgang mit dem Ausdruck finden sich im Dokument "Startkartendruck")
  - Ausgef�llte Zusatzwertung-Checklisten (ohne Zeiten und Bahnen): Ausgef�llte Zusatzwertung-Checklisten - die Fehler Zeit und Bahn sind jedoch leer

![Drucken: Zusatzwertung-Liste](panel-drucken-hlw-liste.png)

- Vordrucke:
  - Startkarten: Leere Startkarten
  - Zusatzwertung-Startkarten: Leere Zusatzwertung-Startkarten
  - Zusatzwertung-Checkliste: Leere Zusatzwertung-Checkliste
  - Zieleinlaufkarten: Leere Zieleinlaufkarten
  - Fehlermeldekarten: Leere Fehlermeldekarten

![Drucken: Vordrucke](panel-drucken-vordrucke.png)

Einige Druckfunktionen werden durch die Ausgabefilter beeinflusst. Genaueres dazu erfahren Sie unter [Ausgabefilter](#Ausgabefilter). Wird eine Druckfunktion gerade durch einen Ausgabefilter beeinflusst, wird dies durch ein Symbol mit einem Auge dargestellt.

**Tipp 1:** Zur Unterst�tzung beim Drucken von Ergebnissen und Protokollen werden unvollst�ndige Eingaben, d.h. fehlende Zeiten bzw. Zusatzwertungspunkte, durch ein gelbes Ausrufezeichen signalisiert.

**Tipp 2:** Die Startkarten f�r den Schwimmwettkampf und die Zusatzwertung werden so gedruckt, dass die Startkarten einer Bahn hintereinander liegen.

**Tipp 3:** Wenn Ausgabefilter aktiv sind, werden Funktionen, die davon betroffen sind, mit einem Symbol "Auge" markiert.
