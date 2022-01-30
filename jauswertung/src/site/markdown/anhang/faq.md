# FAQ

## Was bedeuten die unterschiedlichen Farben in Eingabefeldern?

Gelb markiert Felder m�ssen ausgef�llt werden. Rot markierte Felder enthalten fehlerhafte Angaben.

## JAuswertung wird w�hrend der Ausf�hrung langsamer und belegt mehr Speicher. Woran liegt das?

JAuswertung richtet einige Funktionen erst dann ein, wenn Sie zum ersten mal ben�tigt wurden. Dies reduziert zum Einen die Zeit, die zum Starten ben�tigt wird, und zum Anderen auch den Speicherbedarf w�hrend des Betriebs. Jedes mal, wenn Sie eine Funktion aufrufen, die Sie bisher nicht ben�tigt haben, wird diese eingerichtet und belegt ab diesem Zeitpunkt zus�tzlichen Arbeitsspeicher. Langsamer wird die Anwendung normalerweise nur, wenn der zur Verf�gung stehende Arbeitsspeicher ausgesch�pft ist.

## Ich habe einen Fehler gefunden, wo kann ich diesen melden?

Fehler im Programm k�nnen Sie an [info@dennisfabri.de](mailto:info@dennisfabri.de) schicken oder im Forum auf der Webseite melden.

## Wie kann ich die Warnung bei hohen Zeiten abschalten?

�ber den Men�punkt Extras->Option k�nnen Sie im Register Allgemein die Warnung abschalten.

## Welche Tastaturk�rzel stehen bei der Eingabe zur Verf�gung?

Antwort: Bei der Zeiten- und Laufzeiteneingabe k�nnen direkt Strafen vergeben werden: "n" setzt nur solange keine Zeit eingetragen wurde "Nicht Angetreten", "d" setzt immer "Disqualifiziert", "p" �ffnet ein Fenster zur Strafpunkteeingabe und "#" l�scht immer eine Strafe.

Bei der Zeiten-, Laufzeiten- und Zusatzwertung-Eingabe k�nnen Sie zwischen den einzelnen Zeilen durch dr�cken der Tasten Pfeiltaste oben und unten wechseln. Mit "Strg + Enter" dr�cken Sie den Button "weiter".
Bei der Laufzeiteneingabe k�nnen Sie mit den Pfeiltasten oben und unten zwischen den einzelnen Bahnen wechseln, sind Sie in der ersten oder letzten belegten Bahn angekommen, k�nnen Sie durch erneutes Dr�cken der jeweiligen Pfeiltaste zum n�chsten Lauf wechseln.

## Beim Seriendruck mit Microsoft Word werden die Punkte teilweise mit sehr vielen Nachkommastellen angegeben. Ist das ein Fehler?

Jein, es ist ein Fehler in Microsoft Word, der mit der Darstellung von Dezimalzahlen in Computern zusammenh�ngt.

Umgehung des Fehlers: Exportieren Sie die Ergebnisse von JAuswertung in eine CSV-Datei. Diese kann mit Microsoft Word ebenfalls als Datenquelle genutzt werden. Da hier die Punkte als Text gespeichert werden, hat Microsoft Word keine Probleme mit der Darstellung, da diese bereits von JAuswertung korrekt vorgenommen wurde.

**Erkl�rung:** Microsoft Word ignoriert beim Seriendruck s�mtliche Formatierungen, die in Excel vorgenommen wurden und stellt die Zahlen so dar, wie Sie gespeichert wurden. Leider k�nnen nicht alle Dezimalzahlen exakt gespeichert werden, so dass die n�chstm�gliche Zahl gespeichert wird. Z.B.:

- 1320,64: 1320,6400000000001
- 1257,31: 1257,3099999999999

Die Zahlen unterscheiden sich um ca 0,0000000000001 vom richtigen Wert, was normalerweise v�llig ausreichend ist. Bei der Anzeige sieht es allerdings nicht so gut aus.

## JAuswertung kann einige Excel-Dateien nicht �ffnen. Mit Excel ist das problemlos m�glich.

JAuswertung nutzt ein freies Programm, um Excel-Datei zu lesen. Daher kann es zu Problemen mit einigen Elementen kommen. Zu diesen geh�ren

- Bilder,
- Diagramme,
- Formulare und
- einige Formeln und Makros.

Sollten Sie Probleme haben, speichern Sie die Datei einfach als CSV-Datei. Dieses Format bereitet weniger Probleme.

## Beim PDF-Export zeigt mir die Statusanzeige immer eine Seite mehr an, als anschlie�end im PDF-Dokument enthalten sind.

Die Seiten werden nacheinander geschrieben. Dabei wird jeweils gepr�ft, ob auf eine Seite etwas ausgegeben werden muss. W�hrend dieser �berpr�fung wird eine Seite mehr angegeben, als anschlie�end im Dokument erscheinen.

Diese Anzeige bleibt so lange bestehen, bis alle Daten geschrieben wurden. Es ist also nicht die �berpr�fung, die an dieser Stelle die meiste Zeit beansprucht.

## JAuswertung kann Excel-Dateien nicht �ffnen, die mit Office 95 oder �lter erstellt wurden.

Microsoft hat mit der Version 97 ein neues Dateiformat eingef�hrt. Speichern Sie ihre Dateien einfach vor dem Import in JAuswertung im Format, dass mit Excel 97-2003 kompatibel ist.

## JAuswertung kann Excel-Dateien nicht �ffnen, die mit Office 2007 erstellt wurden.

Microsoft hat mit dieser Version ein neues Dateiformat eingef�hrt. Speichern Sie ihre Dateien einfach vor dem Import in JAuswertung im alten Format. Dieses k�nnen Sie an der Endung ".xls" erkennen.

## Anstatt der normalen Funktion hat sich ein Fenster mit dem Titel "Bug-Report" ge�ffnet. Was hat das zu bedeuten?

Sie sind leider auf einen Fehler in JAuswertung gesto�en. Das Fenster erm�glicht es Ihnen die internen Fehlermeldungen zu speichern. Wenn Sie mir diese Datei per Email schicken, erleichtert mir das den Fehler zu beseitigen.

## �bertr�gt der "Bug-Report" automatisch private Daten ins Internet?

Der "Bug-Report" �bertr�gt gar keine Dateien ins Internet. Es werden ausschlie�lich Informationen gesammelt, die Sie in eine Datei speichern k�nnen.

## Enth�lt die Datei, die im "Bug-Report" gespeichert wurde, private Daten?

Der Bug-Report speichert eine Datei, in unter anderem der Informationen des Systems (Prozessor, Arbeitsspeicher etc.) und der aktuelle Wettkampf enthalten sind. Dies sind die einzigen privaten Daten, die gespeichert werden. �ber den Knopf "Anonymisieren" k�nnen Sie verhindern, dass diese Informationen gespeichert werden. Die Datei enth�lt dann ausschlie�lich Daten, die sich direkt auf den Fehler beziehen. Beachten Sie aber bitte, dass mir alle zus�tzlichen Informationen helfen k�nnen, den Fehler zu beseitigen. Dar�ber hinaus werden die Daten an Dritte **nicht weitergeleitet** und nach der Beseitigung des Fehlers **gel�scht**.
