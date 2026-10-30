---
name: structured-review
description: Run a scoped, tracked review of one target in JAuswertung — a Maven module or a folder inside one — along one of seven lenses (Architecture, Clean code, Tests, Test coverage, Threading, Internationalisation, File compatibility; not every lens applies to every module). Delegates the actual review to the "review" skill or to a deterministic tool (PIT, i18n-missing-keys.sh), logs target/scope/type/date/commit to docs/reviews/README.md, and offers to file findings in docs/issues, docs/chores or docs/duplication. Use when the user wants to review a module or folder, or asks "what's due for review" / "review coverage" in this repo.
---

# Structured Review

Orchestrates review coverage of JAuswertung by Maven module (or a folder inside one) along a single review lens per
run. This skill never judges code itself; it resolves a target, a scope, a type, and a baseline, delegates to the
mechanism defined for that type in `REVIEW_TYPES.md`, logs the result, and offers to file the findings. One type per
invocation.

## Step 1 — Pick target and type

**If the user named a target**, use it directly. Otherwise enumerate candidates and ask the user to pick (a short
free-text prompt is fine — don't force a 4-option menu on a long list):

- **Scope `module`**: a module from the root `pom.xml` `<modules>` (plus `jauswertung-setup` from the `setup`
  profile). Target = the module name, e.g. `jauswertung-data`.
- **Scope `folder`**: any directory under `<module>/src/`, e.g.
  `jauswertung-io/src/main/java/de/df/jauswertung/io/portal`. Useful for `jauswertung-io`, which is too large to
  review in one pass. Target = the repo-relative path. The same path can be reviewed as a folder and its module as a
  module over time, as different scopes with different baselines.

Then ask which **type**: Architecture, Clean code / best practices, Tests, Test coverage, Threading,
Internationalisation, or File compatibility. Look up the target module's class in `REVIEW_TYPES.md` → *Module
classification* and check the type's applicability there — if the user asks for a type that is N/A for that class,
say so (with the reason given) and ask them to pick a different type rather than attempting it. Exactly one type per
run.

When listing candidates, cross-reference the log (Step 5) so you can show what's never been reviewed or is most stale
for the chosen type — but only if the user didn't already name a target.

## Step 2 — Resolve the scope pathspec

- **module**: `<module>/`
- **folder**: `<path>/` (git pathspec naturally includes subfolders)

Generated or binary content inside the pathspec (`target/`, `.xlsx`/`.xls`/`.wk` fixtures,
`src/test/resources/compatibility/**`) is not reviewed for code standards; File compatibility looks at the
compatibility fixtures only to confirm they were not changed.

## Step 3 — Resolve the baseline (fixed point)

Skip this step entirely for **Test coverage** — it's a point-in-time tool run, not a diff (see `REVIEW_TYPES.md` →
*Baseline applicability*). Internationalisation's script also runs on the current tree, but its standards pass is
diff-based, so it uses a baseline.

For every other type, look up the target (matching path, scope, **and type**) in the log (Step 5).

- **Found**: default the baseline to that entry's logged commit. Tell the user the default and let them override it
  (e.g. "rules changed since then, review since `<other-ref>` instead").
- **Not found** (first-ever review of this target/scope/type): there's no sensible default — ask the user what
  commit/tag/date to diff from. If they want a full review of the current state rather than a review of recent work,
  use the repository's root commit as the baseline (`git rev-list --max-parents=0 HEAD`) — diffing from it against
  `HEAD`, scoped to the target's pathspec, turns "review everything" into an addition-only diff the `review` skill's
  diff-based tooling can consume, with every current line appearing as added content.

Before invoking the review mechanism, check whether there's anything to review: `git diff <baseline>...HEAD --
<scope>` — if empty, report "no changes in `<target>` since the last `<type>` review (`<date>`)" and stop. Do not log
a no-op review.

## Step 4 — Run the review

Look up the type's mechanism in `REVIEW_TYPES.md` and follow it. Where that mechanism is a "filtered Standards-axis
pass" of the `review` skill, follow that skill's own process (fixed point → spec source → standards source → parallel
Standards/Spec sub-agents → aggregate) with three adjustments:

- (a) Scope every diff/log command to the pathspec from Step 2 (`git diff <baseline>...HEAD -- <scope>`, not the whole
  repo).
- (b) Narrow the Standards sub-agent's brief to only the `AGENTS.md` sections named for this type; it should ignore
  standards violations outside that lens even if visible in the diff. Also tell it which existing entries in
  `docs/issues/`, `docs/chores/` and `docs/duplication/` already cover a finding, so it cites them instead of
  reporting them as new.
- (c) Spec source: this repo has no `docs/agents/issue-tracker.md` and no issue numbers in commits — specs are the
  files in `docs/issues/` and `docs/chores/`, which are deleted once fixed. Run
  `git log --name-status <baseline>..HEAD -- docs/issues docs/chores` and, for every issue/chore file added, changed,
  or deleted by a commit that also touches the pathspec, use its content (`git show <sha>^:<file>` for deleted ones)
  as the spec. If there is none, skip the Spec sub-agent without asking and note "no spec available" — a module
  review is usually not tied to one feature.

When the mechanism is a deterministic tool (PIT, `scripts/i18n-missing-keys.sh`), run it directly per
`REVIEW_TYPES.md`.

Present the resulting report to the user.

## Step 5 — Log the review

The log is `docs/reviews/README.md`. Update the row for this Target+Scope+Type if one already exists (overwrite its
Date and Commit in place), otherwise append a new row. Never keep more than one row per Target+Scope+Type combination:

- **Target**: the module name or the folder path reviewed.
- **Scope**: `module` or `folder`.
- **Type**: one of the types from Step 1.
- **Date**: today's date (`date +%Y-%m-%d`), not a value you already know from context.
- **Commit**: the short SHA of `HEAD` at review time (`git rev-parse --short HEAD`).

## Step 6 — File the findings

After the report, offer to file the confirmed findings. Each finding becomes a self-contained file listed in the
matching README's table, following that README's rules:

- `docs/issues/` — defects: wrong behavior, wrong results, a rule violation with user-visible effect.
- `docs/chores/` — clean-ups that are not bugs (comments, logging, class design, missing tests, convention-only
  rules).
- `docs/duplication/` — duplicated code or rules.

Before adding an entry, check that no existing entry already covers it; extend that one instead. Rate every new row
with a severity and size as defined in the respective `README.md` and keep the table ordered by severity, then size.
Filing is a separate change from the review itself — do not fix findings as part of this skill.

## Notes

- This skill has no review judgement of its own for the diff-based types — all findings come from the `review`
  skill. Test coverage has no judgement at all — it reports a tool's raw output. If the `review` skill's process
  changes, Step 4's adjustments are the only part that need re-checking for compatibility.
- The log is a current-status table, not a history — each Target+Scope+Type keeps only its latest review. It answers
  "when was this last reviewed and against what commit," not "how has this trended over time".
- Sibling projects (`jutils-legacy`, `AlphaServer`, `alphatimer`, …) are out of scope; a finding that belongs there
  is reported, not filed here.
- See `REVIEW_TYPES.md` for the full type-to-mechanism mapping and applicability rules.
