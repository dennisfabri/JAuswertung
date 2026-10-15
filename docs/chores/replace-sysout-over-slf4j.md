# Replace sysout-over-slf4j

`uk.org.lidalia:sysout-over-slf4j` redirects `System.out`/`System.err` to SLF4J. Its last release is from about
2016–2017 and it has no maintainer. grep finds one Java file in the project using it.

Replace it with a small own class that installs `System.setOut`/`System.setErr` with a `PrintStream` forwarding to an
SLF4J logger, or drop it once the calls described in [console-output-and-printstacktrace.md](console-output-and-printstacktrace.md)
are gone.

## Done when

The dependency and its version property are removed from the root `pom.xml`, and console output still ends up in the log.
