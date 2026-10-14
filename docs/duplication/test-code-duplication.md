# Test code is heavily duplicated

- 235 championship result test classes (`jauswertung-io/src/test/java/.../test/results/dm00…dm08`, `dm1999`, `dsm06`,
  about 52,000 lines) differ only in `FILE`/`MALE`/`AK`/rulebook.
- The fixture builders in `FormelILSFinalsTest`, `FormelILSFinalsDisqualificationTest`,
  `FormelILSFinalsWithdrawTest` and `FormelILSOutdoorFinalsTest` repeat about 190 lines (CPD 1,755/1,357 tokens).

## Done when

The championship tests are one `@ParameterizedTest` with `@MethodSource`, and the ILS finals fixtures are a shared
helper in `test/util`.
