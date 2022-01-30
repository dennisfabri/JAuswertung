# FAQ

## Was bedeuten die unterschiedlichen Farben in Eingabefeldern?

Gelb markiert Felder müssen ausgefüllt werden. Rot markierte Felder enthalten fehlerhafte Angaben.

## JAuswertung wird während der Ausführung langsamer und belegt mehr Speicher. Woran liegt das?

JAuswertung richtet einige Funktionen erst dann ein, wenn Sie zum ersten mal benötigt wurden. Dies reduziert zum Einen die Zeit, die zum Starten benötigt wird, und zum Anderen auch den Speicherbedarf während des Betriebs. Jedes mal, wenn Sie eine Funktion aufrufen, die Sie bisher nicht benötigt haben, wird diese eingerichtet und belegt ab diesem Zeitpunkt zusätzlichen Arbeitsspeicher. Langsamer wird die Anwendung normalerweise nur, wenn der zur Verfügung stehende Arbeitsspeicher ausgeschöpft ist.

## Ich habe einen Fehler gefunden, wo kann ich diesen melden?

Fehler im Programm können Sie an [info@dennisfabri.de](mailto:info@dennisfabri.de) schicken oder im Forum auf der Webseite melden.

## Wie kann ich die Warnung bei hohen Zeiten abschalten?

Über den Menüpunkt Extras->Option können Sie im Register Allgemein die Warnung abschalten.

## Welche Tastaturkürzel stehen bei der Eingabe zur Verfügung?

Antwort: Bei der Zeiten- und Laufzeiteneingabe können direkt Strafen vergeben werden: "n" setzt nur solange keine Zeit eingetragen wurde "Nicht Angetreten", "d" setzt immer "Disqualifiziert", "p" öffnet ein Fenster zur Strafpunkteeingabe und "#" löscht immer eine Strafe.

Bei der Zeiten-, Laufzeiten- und Zusatzwertung-Eingabe können Sie zwischen den einzelnen Zeilen durch drücken der Tasten Pfeiltaste oben und unten wechseln. Mit "Strg + Enter" drücken Sie den Button "weiter".
Bei der Laufzeiteneingabe können Sie mit den Pfeiltasten oben und unten zwischen den einzelnen Bahnen wechseln, sind Sie in der ersten oder letzten belegten Bahn angekommen, können Sie durch erneutes Drücken der jeweiligen Pfeiltaste zum nächsten Lauf wechseln.

## Beim Seriendruck mit Microsoft Word werden die Punkte teilweise mit sehr vielen Nachkommastellen angegeben. Ist das ein Fehler?

Jein, es ist ein Fehler in Microsoft Word, der mit der Darstellung von Dezimalzahlen in Computern zusammenhängt.

Umgehung des Fehlers: Exportieren Sie die Ergebnisse von JAuswertung in eine CSV-Datei. Diese kann mit Microsoft Word ebenfalls als Datenquelle genutzt werden. Da hier die Punkte als Text gespeichert werden, hat Microsoft Word keine Probleme mit der Darstellung, da diese bereits von JAuswertung korrekt vorgenommen wurde.

**Erklärung:** Microsoft Word ignoriert beim Seriendruck sämtliche Formatierungen, die in Excel vorgenommen wurden und stellt die Zahlen so dar, wie Sie gespeichert wurden. Leider können nicht alle Dezimalzahlen exakt gespeichert werden, so dass die nächstmögliche Zahl gespeichert wird. Z.B.:

- 1320,64: 1320,6400000000001
- 1257,31: 1257,3099999999999

Die Zahlen unterscheiden sich um ca 0,0000000000001 vom richtigen Wert, was normalerweise völlig ausreichend ist. Bei der Anzeige sieht es allerdings nicht so gut aus.

## JAuswertung kann einige Excel-Dateien nicht öffnen. Mit Excel ist das problemlos möglich.

JAuswertung nutzt ein freies Programm, um Excel-Datei zu lesen. Daher kann es zu Problemen mit einigen Elementen kommen. Zu diesen gehören

- Bilder,
- Diagramme,
- Formulare und
- einige Formeln und Makros.

Sollten Sie Probleme haben, speichern Sie die Datei einfach als CSV-Datei. Dieses Format bereitet weniger Probleme.

## Beim PDF-Export zeigt mir die Statusanzeige immer eine Seite mehr an, als anschließend im PDF-Dokument enthalten sind.

Die Seiten werden nacheinander geschrieben. Dabei wird jeweils geprüft, ob auf eine Seite etwas ausgegeben werden muss. Während dieser Überprüfung wird eine Seite mehr angegeben, als anschließend im Dokument erscheinen.

Diese Anzeige bleibt so lange bestehen, bis alle Daten geschrieben wurden. Es ist also nicht die Überprüfung, die an dieser Stelle die meiste Zeit beansprucht.

## JAuswertung kann Excel-Dateien nicht öffnen, die mit Office 95 oder älter erstellt wurden.

Microsoft hat mit der Version 97 ein neues Dateiformat eingeführt. Speichern Sie ihre Dateien einfach vor dem Import in JAuswertung im Format, dass mit Excel 97-2003 kompatibel ist.

## JAuswertung kann Excel-Dateien nicht öffnen, die mit Office 2007 erstellt wurden.

Microsoft hat mit dieser Version ein neues Dateiformat eingeführt. Speichern Sie ihre Dateien einfach vor dem Import in JAuswertung im alten Format. Dieses können Sie an der Endung ".xls" erkennen.

## Anstatt der normalen Funktion hat sich ein Fenster mit dem Titel "Bug-Report" geöffnet. Was hat das zu bedeuten?

Sie sind leider auf einen Fehler in JAuswertung gestoßen. Das Fenster ermöglicht es Ihnen die internen Fehlermeldungen zu speichern. Wenn Sie mir diese Datei per Email schicken, erleichtert mir das den Fehler zu beseitigen.

## Überträgt der "Bug-Report" automatisch private Daten ins Internet?

Der "Bug-Report" überträgt gar keine Dateien ins Internet. Es werden ausschließlich Informationen gesammelt, die Sie in eine Datei speichern können.

## Enthält die Datei, die im "Bug-Report" gespeichert wurde, private Daten?

Der Bug-Report speichert eine Datei, in unter anderem der Informationen des Systems (Prozessor, Arbeitsspeicher etc.) und der aktuelle Wettkampf enthalten sind. Dies sind die einzigen privaten Daten, die gespeichert werden. Über den Knopf "Anonymisieren" können Sie verhindern, dass diese Informationen gespeichert werden. Die Datei enthält dann ausschließlich Daten, die sich direkt auf den Fehler beziehen. Beachten Sie aber bitte, dass mir alle zusätzlichen Informationen helfen können, den Fehler zu beseitigen. Darüber hinaus werden die Daten an Dritte **nicht weitergeleitet** und nach der Beseitigung des Fehlers **gelöscht**.
