# Review Log

Written by the `structured-review` skill (`.agents/skills/structured-review/`). One row per Target+Scope+Type, holding
its most recent review only. Each row's Commit is the HEAD that review covered up to — the next review of the same
Target+Scope+Type defaults its baseline to that commit (except Test coverage, which has no baseline — see
`.agents/skills/structured-review/REVIEW_TYPES.md`). Findings are filed in `docs/issues/`, `docs/chores/` and
`docs/duplication/`, not here. Keep this README with its table even when it is empty.

| Target | Scope | Type | Date | Commit |
| --- | --- | --- | --- | --- |
| jauswertung-data | module | Architecture | 2026-10-30 | 75f357c5 |
