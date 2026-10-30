# daten contains classes that are not part of the model

*Packages in jauswertung-data* (`AGENTS.md`) allows only model classes in `de.df.jauswertung.daten`. Candidates that
look like something else (paths relative to `jauswertung-data/.../daten/`):

| Class | Why it is a candidate |
| --- | --- |
| `misc/BugReport` | Diagnostic report, not competition data; only holds the collected text (collected by `BugReportCollector` in plugins-bugreport) |
| `Mannschaftsmitgliedermeldung` | Wrapper around a `MannschaftWettkampf`, root object of `.wkmm` files — check whether it is a file envelope rather than model |
| `laufliste/OWSelection` | Selection of age group, sex, discipline and round, used by 37 classes outside data, but also stored (`IOUtils` alias) — check whether it is model or a key/DTO |
| `PropertyConstants` | Constants for the generic property maps of the model; probably model, confirm |

Every class here is serialization-relevant: `BugReport`, `OWSelection` and `Mannschaftsmitgliedermeldung` have XStream
aliases (`IOUtils:205,219,239`). Moving one out of `daten` needs it added to the `allowTypes` whitelist and must keep
its alias — prove it with a test that loads an existing file.

## Done when

Each candidate is either confirmed as model (and removed from this list) or moved to a package outside `daten` without
breaking existing files.
