# Round competition removes the wrong discipline

## Finding

`ResultUtils.createCompetitionFor` is meant to remove all other rounds from the copy ("Remove disciplines before
participants for performance reasons"), but the loop calls `removeDiscipline(t.getId())` – the selected round –
instead of `removeDiscipline(dx.Id)`. Results are still correct, because the selected round is read from the original
competition and the round list of the copy is cleared at the end; only the intended speed-up is lost.

## Done when

The loop removes the other rounds, and the round competitions behave as before (compatibility exports unchanged).
