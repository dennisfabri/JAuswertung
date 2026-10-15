# Remove the Mersenne Twister

`RandomUtils.getRandomNumberGenerator()` (`jauswertung-data/.../util/RandomUtils.java`) returns commons-math3's
`MersenneTwister` wrapped in a `RandomAdaptor`. It is the only use of commons-math3 (`jauswertung-data/pom.xml`, version
in the root `pom.xml`).

The generator brings no benefit here. It shuffles swimmers, organisations and groups during heat generation
(`Laufliste:907,1457,1491`), draws the random selection in `JSelectionWizard:300`, creates the 6-character team-member
registration id (`MannschaftWettkampf:56`) and dummy heats for electronic timing (`ETDLRGStrategy:78`), plus test data
in `Namengenerator` and `ErzeugeWettkaempfe` (misc). Its long period does not matter for lists of a few dozen entries,
and the default constructor seeds it from `currentTimeMillis() + identityHashCode`, so it cannot reach more sequences
than `java.util.Random` in practice. The id is not a secret (the scan only uses it to detect a different competition),
so no `SecureRandom` is needed either.

It also has a cost: `Laufliste` keeps one static instance shared by all callers, and `MersenneTwister` is not
thread-safe, unlike `java.util.Random`. `JSelectionWizard:171` re-seeds with `System.nanoTime()` before every use,
which adds nothing.

## Done when

`RandomUtils` returns a JDK generator (`new Random()`, or `Random.from(RandomGenerator.of("L64X128MixRandom"))`), the
re-seeding in `JSelectionWizard` is gone, and commons-math3 is removed from both POMs. `FormelILSOutdoorFinalsTest`
generates its heats with a fixed seed (`Laufliste.erzeugen(13)`); check whether its expectations depend on the drawn
heat order and adjust them if the new generator changes it.
