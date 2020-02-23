# Runtime overview

## Compiler
Skripty compiler targets a very simple, stack-based virtual machine. There are
instructions for basic control flow, stack manipulation and JVM method calls.
The format was designed to be easy to run with an interpreter (see below), and
similar enough to JVM bytecode that it could be further compiled to that.

### Resolving call targets
Expressions may define many call targets. The compiler resolves which of them
should be called based on JVM return types of inputs (and optionally,
SkriptTypes of them). There are many reasons why this is necessary:

* Expressions may have patterns that take different amounts of inputs
  * They can be mapped to different JVM methods, no need to pass nulls around!
* Some input slots may accept many types
  * Specialized implementations *without* tons of if-statements
* SkriptTypes are often mapped to many JVM types
* Directly calling methods that take primitive types helps avoid boxed types
  * Not very useful with an interpreter, but with JVM bytecode, this is great

## Interpreter
The interpreter executes compiled scripts. It is simply a loop with a bunch of
conditions that implement the necessary instructions.

### Calling Java
Most functionality provided to scripts is implemented in Java methods that the
interpreter calls. This could be done with reflection, but MethodHandles
provide a lower-level (and generally better) API for calling arbitrary methods.