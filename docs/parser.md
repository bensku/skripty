# Parser overview

## Terminology
* Expression
  * A function/subroutine that defines the syntax used to call it
  * Receives zero or more input values ("inputs"), may return one value
  * From script's perspective, inputs are parameters
  * But they do not always directly map to method parameters in Java side... (TODO link to more info)
* Statement
  * Contents of a single line
  * An expression that returns nothing (SkriptType#VOID)
* Block
  * A statement that contains a list of other statements
  * Inner statements are executed sequentially when a block is entered
  * May also have *title expression* that allows it to control when it is
    entered

## Section parsing
Section parser figures out boundaries of statements and blocks based on line
breaks and indentation. As an example, let's take a look at a small, imaginary
script:

```
if "foo" is "foo":
    if "bar" is "bar":
        print "Strings are equals!"
```

Time complexity of the section parser is O(n), nothing really surprising here.

## Block parsing, scopes
Once a script has went through the section parser, we can start parsing
expressions. In different contexts, different expressions may be available:

```
if "foo" is "foo":
    print "Strings are equals!"
```

The title expression here is a condition that determines whether the block
should be entered. It returns values of a special type (ScopeEntry#TYPE)
that should *not* be exposed to scripts.

Skripty avoids this by using expression parsers with different sets of
expressions for titles and contained statements. Scopes would also allow
limiting child expressions in certain blocks, but that is currently not done.

Time complexity of block-level parsing depends on that of the expression
parser. Number of calls to the expression parser scales linearly.

## Expression parsing
The expression parser is the 'magic' of Skripty that turns English sentences
into abstract syntax tree (AST) nodes. It it also just a very specialized
pattern matching engine.

### Input and return types
Expression define accepted types (SkriptType) of their inputs, and their own
return types. The expression parser respects these, and will not generate
an AST that has expressions taking inputs of wrong types.

### The patterns...
Patterns of Skripty consist of *literal parts* and *inputs*. Literal parts are
just text that must be found in the matched strings. Inputs indicate the places
where more expressions (or literals, more about that later) should be.

For example, let's take a look at this pattern:
```
{0} is {1}
```
It indicates that there should be an expression at start of the matched string.
It should be immediately followed by *exactly* <code> is </code>. Finally,
there should be another expression. If any of these three conditions is not
met, this pattern does not match a string.

### ... and how to match them
But how exactly we match patterns *inside* patterns? Parsing expressions that
are used as inputs (of inputs of inputs) is challenging. Skripty's approach is
to attempt parsing all expressions that *might* fit for every input.

In practice, this means filtering out patterns that do not have literal
that matches the input string at current position as their first parts.
The parser will attempt to match them all against the input string.

This ignores the expressions with patterns starting with inputs. To support
them, Skripty does not immediately return all successfully matched expressions.
Instead, it will look up expressions that have literals matching the string
*after* the initial results as their second parts. Their parsing will be
attempted, with already parsed expressions used as first inputs.

Finally successes are pooled together. The ones that have correct return types
are returned; others are discarded, unless a special parser option was
provided.

### Looking up the expressions
Finding expressions based on their first or second parts is done by using
two special-purpose radix-2 trees. They allow getting all values with keys
that are the given input starts with.

For efficiency and simplicity of implementation, the trees operate on byte
arrays of UTF-8 string data. Because of this, the expression parser also
operates on raw bytes - block parser calling it encodes strings as needed.

Currently, Java's built-in UTF-8 encoders and decoders are used to convert
between Strings and byte arrays. Their performance seems acceptable, but
in ideal world Skripty would do *everything* on byte arrays.

### Time complexity
Finding all potentially matching patterns from a radix tree has time complexity
of O(k), where k is length of longest key in the tree. It is does not correlate
with amount of expressions registered.

Time complexity of expression parsing is O(n^d). n is number of inputs taken
by the expression; d represents how many levels of nesting the input has.