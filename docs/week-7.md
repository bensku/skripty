# Progress report - week 7
I have a very simple scripting language implemented on top of well-documented
and (mostly) tested library modules. Skripty is still missing user manual; I
decided to write that at last possible time to avoid rewrites due to changed
or added features. I'll have it done before next Friday, in any case.

Now, what could I do between now and then, in addition to writing
documentation? I have a few candidates that I might spend time on:
* Compiler testing - currently, IR compiler is largest part of Skripty that
  is not properly unit-tested; its correct functionality is also critical
* Simpleskript features - it is *very* bare-bones language that lacks e.g.
  variables; probably not Turing-complete, even
  - I/O expressions (annotation API is good, should be very easy)
  - Variables (quite easy, requires adding "block state" to literal parsers)
  - More control flow to have uses for variables (e.g. loops)
* Something bigger - list support or generic types?
  - Then again, probably not

I will avoid making too disruptive changes to codebase, because the deadline is
absolute. Breaking what works and is (in my opinion) a rather large project
already would be pointless at this point.