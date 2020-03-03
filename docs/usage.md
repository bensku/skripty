# Simpleskript usage
Simpleskript is a simple scripting language built on top of Skripty. Due to
time constraints, it lacks features that Skripty has support for. In particular,
there are no loops, variables, or any actually useful syntaxes. That being said,
it does showcase most important APIs of Skripty.

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

## Syntaxes
* crash: throws an exception
* A equals B: returns a boolean that indicates if A and B are equal
* print A: prints A to console
* current time: returns current time