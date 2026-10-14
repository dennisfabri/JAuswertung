# Overall scoring scales points with and without "no rating"

In `GesamtwertungWettkampf.java`, `scalePointsInternational` (`:143-187`) skips results with `hasKeineWertung()`,
while the otherwise identical `scalePointsMedaillen` (`:190-232`) includes them.

## Decide

Whether the difference is intended. Then extract one `toPlacementCompetition(formelId, includeNoRating)` used by both.
