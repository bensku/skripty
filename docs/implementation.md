## Skripty Implementation
Skripty is designed to be modular from start. The core (at root of repository)
contains minimum code to tie them together, as well as some code needed by all
other components. Parser and runtime code are completely separate; should you
need need no parsing, just not including the parser dependency is a valid
strategy.

### Parser
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

#### The Expression Parser
The expression parser is hands down most complex of the parsers described
above. Actually, it is probably the most complex part of Skripty.

The expressions are matched using *patterns* that consist of literal and
input parts. Literal parts must match the input string exactly. Input parts
take *other expressions*, and are matched recursively.

The parser begins any expression parsing operation by looking for expressions
with first literal part matching the input string. After that, it recursively
parses expressions to provide inputs, and matches the literal parts after them
for each potential input.

For efficiency, literal part lookups are done to radix trees. Lookups to them
have time complexity of O(k), where k is max length of first literal part of
any expression.

Skripty's radix tree is a radix-2 tree that uses UTF-8 encoded strings
(byte arrays) as keys. This made supporting Unicode with good performance
easier than it would have been with j.l.String. The tree does *not* support
deletion of values, because there is no need for that.

### Runtime
Skripty's runtime consists of a compiler and an interpreter. The compiler
generates flat IRs (intermediate representations) of scripts it compiles.

Executing scripts is currently only possible with a simple interpreter.
It is not very fast, but probably more than good enough for a scripting
language. Writing a compiler with JVM bytecode as a target would
significantly improve performance, but is not trivial to implement.