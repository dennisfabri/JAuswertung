# Gender exists twice with the same name

`jauswertung-io/src/main/java/org/lisasp/competition/base/api/type/Gender.java` and
`jauswertung-portal-adapter/src/main/java/org/lisasp/competition/base/api/type/Gender.java` are identical (20 lines).
portal-adapter depends on io, so the class is on the classpath twice and the first one wins.

## Done when

The portal-adapter copy is deleted.
