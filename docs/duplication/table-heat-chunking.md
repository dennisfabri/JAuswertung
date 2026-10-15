# Heat chunking in TableHeatUtils is copied three times

`jauswertung-swing/.../TableHeatUtils.java:141-188`, `866-913` and `1000-1036` split swimmers into heats of `bahnen`
lanes (CPD 271/211 tokens); merging nearly empty tables is copied at `:205-266` and `:330-392`. No behaviour
difference yet (only `Arrays.fill` vs. a loop).

## Done when

Private helpers `chunkIntoHeats(lsi, bahnen)` and `mergeSparse(result, bahnen)` replace the copies.
