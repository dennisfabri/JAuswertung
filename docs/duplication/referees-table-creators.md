# Referee table creators are copied

`jauswertung-print/.../print/RefereesTableCreator.java` and `RefereesTableCompactCreator.java` (`:33-67`, CPD 309
tokens) are copies. Both hard-code the German text "Stufe" instead of using `I18n`.

## Done when

The shared part is one method, and "Stufe" is translated.
