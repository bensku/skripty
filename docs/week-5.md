# Progress report - week 5
I had less time than I had expected to work on Skripty this week. Even so,
I now have a usable (though *very* simplistic) scripting language with syntax
that mimics English. Getting this far involved a lot of debugging, writing
tests and adding features that I decided would be critical in middle of the
project.

I'm quite happy about quality of Skripty's library parts. There is only one
*major* missing feature (issue #4, parser error handling), and even it is not
critical considering the project goals. Next week, I'll try to tackle it and
extend simpleskript (the application part) to do more 'cool' things.

If there is time, I'll work on improving test coverage (issue #3, runtime
testing). I might also try open a can of worms and try to implement duck typing
and/or generic types (issue #7). Further performance testing could also be
conducted, but comparing performance of programming languages is *very*
difficult and the raw numbers without any comparisons are not that useful.

Besides working on the application, I'll need to spend some time to get
required documents (*especially* the usage) finished in next weeks.
Especially important is that I explain my usage of annotations
(i.e. reflection) to provide a high-level API on top of low-level one.

Actually, same goes for usage of method handles thorough Skripty. Their API is
consists of mostly native methods that I cannot replace myself. I had not
considered this a problem because they are much lower-level compared to
reflection API. I realize that this might be mistaken assumption, but it is
practically impossible to implement Skripty's design of multiple call targets
without using method handles, unless extensive bytecode generation is needed.