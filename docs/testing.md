## Testing document
Skripty is tested primarily automatically. Because there is no GUI involved,
it has been reasonable easy thus far.

## Unit/integration tests
Unit and integration tests are written in Java using jUnit 5. Gradle runs *all*
tests every time the project is built. The tests are also run for all pushes
in Github Actions CI.

## Performance testing
Performance testing on JVM is tricky, because it usually does JIT compilation.
Hotspot has an interpreter and *three* different JIT compilers:

* C1: compiles fast, but compiled code may be slower
* C2 (opto): compiles slower, but compiled code is faster
* Graal: similar to C2; disabled by default

### JVM warmup
When the code is first used, it is executed with interpreter. If it is used
again, C1 will compile it soon. If a piece of code is used a *lot*, C2 will
compile it. Due to this, the application will start slow, and improve its
performance over time.

Unless the benchmarks mitigate this by "warming up" JVM, they will not produce
useful data on peak performance of the tested code.

### Optimizations
Top-tier JIT compilers will heavily optimize the JVM bytecode. As long as there
are no user-visible changes, they're free to do pretty much anything.
Eliminating a costly calculation whose result is not used *will* happen.
In benchmarks, it is vital to ensure that the JVM does not optimize tested code
away. Failure to do so will, again, not provide useful results.

### Measuring time
Benchmarks measure time it takes the tested code to run. Unfortunately, this
comes with some overhead. It is vital to ensure that your benchmarks are
measuring how fast *your* code is, not how fast System.currentTimeMillis()
happens to be on [your OS](https://pzemtsov.github.io/2017/07/23/the-slow-currenttimemillis.html).

### So...
Skripty uses [JMH](https://openjdk.java.net/projects/code-tools/jmh/)
for performance testing. It has been built by OpenJDK maintainers to solve
the above problems. Considering that I'm not a JVM maintainer, it probably
avoids pitfalls that I'm not even aware of.

There exists a Gradle plugin for JMH. Unfortunately, it is not working that
well for me. Running benchmarks requires commands like this:

```
./gradlew clean jmhJar \<subproject\>:jmh
```
If Gradle daemon is used, it might need to be stopped when changes are made
to benchmarks. It's inconvenient, but writing reliable benchmarks *without*
JMH would be worse.

Benchmarks are not run on Github Actions, because they are very slow.
Besides that, performance testing on virtualized environments does not
necessarily provide reliable results.