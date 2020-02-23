# Skripty Implementation
Skripty is designed to be modular from start. The core (at root of repository)
contains minimum code to tie them together, as well as some code needed by all
other components. Parser and runtime code are completely separate; should you
need need no parsing, just not including the parser dependency is possible.

## Parser
Skripty has a few different parsers for different levels of script parsing.
Usually, scripts will go through them in the following order:

1. SectionParser
   * Creates code blocks based on indentation (like in Python)
   * Splits blocks into statements by using new-line as separator
2. BlockParser
   * Parses titles (first lines) of blocks to find their types
   * Calls correct expression parsers on parsed blocks
3. ExpressionParser
   * Parses individual expressions
   * For more details about the concept, see definition.md
   * For implementation details, read below

For (much) more details, see parser.md and/or parser Javadoc.

### Runtime
Skripty's runtime consists of a compiler and an interpreter. The compiler
generates flat IRs (intermediate representations) of scripts it compiles.

Executing scripts is currently only possible with a simple interpreter.
It is not very fast, but probably more than good enough for a scripting
language. Writing a compiler with JVM bytecode as a target would
significantly improve performance, but is not trivial to implement.

For more details, see runtime.md.