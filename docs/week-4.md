# Progress report - week 4
I finished initial (usable enough) version of the expression parser. I also
implemented the other, simpler, parsing components needed to run scripts.
Having done that, I designed a simple stack-based IR for scripts. This includes
a proof-of concept compiler targeting it, and an interpreter to run it.

By that point, it had become apparent that current APIs provided by Skripty
were far too low level to actually implement scripting languages with. I
started to combat the boilerplate by creating an annotation based API using
Java's reflection APIs. This does not perfectly align with goals of the course,
so I made sure to build it exclusively on public, lower level APIs of Skripty.

Test coverage went down this week due to addition of many new features.
I will work on improving it in coming weeks, while also doing exploratory
testing to catch bugs. Performance testing using JMH has been started, and
more benchmarks will also be added as time permits.

Lastly, I started working on a standalone application which allows running
scripts from files and might include REPL. It will probably be really usable
sometime next week, but I intend to continue expanding on it as the project
goes on.

Overall, I am very satisfied with this weeks' progress. All core library
components are *usable*, and work on application has been started.