# Simpleskript usage
Simpleskript is a simple scripting language built on top of Skripty. Due to
time constraints, it lacks features that Skripty has support for. In particular,
there is no support for advanced control flow such as loops.

There are also some limitations in Skripty that would make building a larger
language unfeasible:

* No type conversions
  * Expressions that want to accept variables need to manually define
    call targets for them
  * Exponentially more call targets needed for multi-argument expressions
* No generic types
  * E.g. equals expression cannot check that both parameters are of same type
    compile-time
  * Will instead cause runtime errors due to missing call targets
* SkriptType array and list literal support
  * Can't build expressions that take more than one value per input easily

All in all, Skripty does satisfy the original design goals. Unfortunately,
that is not enough to build useful languages with. I may continue to project
with potentially interested parties once the course is over. The core is
solid, mostly documented and reasonable well-tested, so it would be shame to
throw it away.

## Starting
At root of this repository, use
```
TERM=dumb ./gradlew simpleskript:run
```
This starts a bare-bones REPL. There may be some input lag caused by Gradle.

Alternatively, you can compile Simpleskript:
```
./gradlew simpleskript:shadowJar
```
and then launch it:
```
java -jar simpleskript/build/libs/simpleskript-all.jar
```

In latter case, you may also run written script files:
```
java -jar simpleskript/build/libs/simpleskript-all.jar test.sk
```
<code>test.sk</code> exists at root of this repository, by the way.
It contains examples of almost all expressions!

## Syntaxes
* crash: throws an exception
* A equals B: returns a boolean that indicates if A and B are equal
* print A: prints A to console
* current time: returns current time
* set A to B: set variable A to string or variable B