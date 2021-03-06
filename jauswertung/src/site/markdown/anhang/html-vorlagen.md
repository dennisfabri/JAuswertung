# HTML-Vorlagen

Der Export von HTML-Dateien sowie die Ausgabe des HTTP-Servers wird durch so genannte XSL-Templates festgelegt. Sie k�nnen auf diese Weise die Ausgabe beliebig anpassen. Der interne Ablauf der Erstellung von HTML-Dateien verl�uft dabei wie folgt:

1. JAuswertung erstellt aus dem aktuellen Wettkampf ein XML-Datei, die alle n�tigen Daten enth�lt.2. �ber einen sogenannten Transformer wird die XML-Datei mit einer XSLT-Datei verkn�pft und so die HTML-Datei erstellt.

Der Aufbau der XML-Dateien entspricht dabei dem der Dateien, die von JAuswertung exportiert werden k�nnen. Wenn Sie also die Ausgabe anpassen wollen, m�ssen Sie nur die entsprechenden Dateien im Unterverzeichnis "xsl" im Installationsordner von JAuswertung entsprechend anpassen.

**Anmerkung 1:** Auf eine genaue Beschreibung zum Umgang mit XSLT wird hier verzichtet, da entsprechende Beschreibungen im Internet in ausreichender Menge und Qualit�t vorhanden sind.

**Anmerkung 2:** Ein Fehler in der XSLT-Datei f�hrt dazu, dass die Transformation nicht durchgef�hrt werden kann. JAuswertung meldet daraufhin, dass die Datei nicht gespeichert werden konnte.