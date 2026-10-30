# Team codes are written and parsed inside Team

Found by the Architecture review of `jauswertung-data` (`docs/reviews/README.md`). `Team`
(`jauswertung-data/.../util/valueobjects/Team.java`) is a value object, but also holds the versioned team-code format
used for team-member registration: `createCode00`/`createCode01` (lines 93-152) write `JA00`/`JA01` codes
(Huffman-compressed, hex-encoded, via `compress`/`decompress`), and `FromCode`/`FromCode00`/`FromCode01` (lines
162-230) parse them. Callers of `FromCode`: `TeamUtils` and `JTeammembersBarcodeScanPanel`
(plugins-teammembersscan); the code is read through `getCode()`.

`docs/duplication/README.md` keeps the two readers separate on purpose; that stays as it is.

## Done when

A class like `TeamCode` holds the format with a `from(String)` and a `to(Team)` method (names following the
surrounding code), `Team` keeps only its data, and the callers use `TeamCode`. Codes created before the change still
parse — cover `JA00` and `JA01` with a test against fixed codes.
