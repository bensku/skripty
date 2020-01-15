# Project definition
Skripty is an attempt to build a scripting language with syntax that *seems*
like natural language. It is written in Java (version 11, probably) and will
run on JVM.

## Origins
I'm current maintainer of [Skript](https://github.com/SkriptLang/Skript),
which is primary source of inspiration for Skripty's syntax. That is where
the similarities end, or so I hope.

Aside of external requirements (e.g. from university), Skript's codebase is
not exactly clean. Large parts of it are poorly (or not) documented, and
autoted test coverage is minimal. Also, some parts are just rather
[hard](https://github.com/SkriptLang/Skript/blob/master/src/main/java/ch/njol/skript/lang/SkriptParser.java)
to maintain, even after years of tinkering with them...

## Basics of Skript(y): the syntax
Skripty aims to provide syntax that *appears* to be like natural English.
For example, the following snippet could be a valid script:

```
set content of file "a.txt" to content of file "b.txt"
show content of file "a.txt"
```

## That was a lie
Skripty doesn't actually do natural language parsing. It would provide way
too unpredictable results and make debugging a rather painful experience.
Besides that, NLP is not very fast and pretty much requires using
third-party libraries due to inherent complexity.

In reality, Skripty's parser is just a fancy, special purpose pattern-matching
algorithm. It allows *expressions* (pretty much functions) to provide their
own, customized syntaxes. In a more traditional programming language, the above
snippet might look like this:

```
content(file("a.txt")).set(content("b.txt"))
show(content(file("a.txt"))
```

And that's it. Beyond the syntax, Skripty is just your standard embedded JVM
scripting language.

## Ugly details: the pattern matching
The expressions are identified by *literal* parts in their pattern.
Let's take the expression used for setting things as an example:
```
set [input 1] to [input 2]
```

To check if a string (a line, for example) matches it, we could do something
like this (pseudocode):
```
function parseSetExpression(line):
    pos = 0
    # Ensure that the line starts with 'set'
    if line[0:3] != 'set
        return
    pos += 3
    # Branch to multiple paths for all possible inputs at slot 1
    for input1,afterPos in parseInput(line, pos):
        # Filter out inputs that take 'to' from us
        if line[afterPos:2] != 'to':
            continue
        afterPos += 2
        for input2,after2 in parseInput(line, afterPos):
            # Filter out inputs that would leave unparsed characters at end
            if after2 != line.length - 1:
                continue
            return [input1, input2]
```

Skripty will contain a generic parser that does this based on *patterns*
defined for the expressions.

## Choosing the right patterns
What I described doesn't describe how Skripty would choose the right patterns.
Just trying them all is an simple, but has O(n) complexity, where n is number
of expressions registered to the parser. This is not ideal.

Instead, we can use a trie to find potential patterns in O(k) time, with k
being the maximum length of an expression. That is *much* better, but also
significantly harder to implement. I will try to get it working, but fall back
to O(n) approach if time runs out.

## Edge cases
### Expressions starting with inputs
Handling this edge case requires us to not just fail when an expression leaves
unparsed characters at end. Instead, we'll try if it could work as first input
to expressions that start with inputs.

## Type safety
Expressions define their return types and acceptable types of inputs. Inputs
with incorrect types should produce error messages.

## Appendix: algorithms likely implemented
A radix tree will probably be used for finding potentially matching patterns.
Beyond that, I expect to get far with just plain arrays.

## Sources: prior Skript parser implementations
* [The original parser](https://github.com/SkriptLang/Skript/blob/master/src/main/java/ch/njol/skript/lang/SkriptParser.java)
  * There can be too much recursion
  * Bug fixes and improvements added every once a while
  * Probably no one but original author understands this one fully
* [skript-parser](https://github.com/SkriptLang/skript-parser)
  * A much more readable parser
  * Uses same techniques as original parser, shares some of its problems
* [skript-next](https://github.com/SkriptLang/skript-next)
  * My experiments with parsing and interpretation
  * *Very* different from original parser
  * Was not very successful in the end