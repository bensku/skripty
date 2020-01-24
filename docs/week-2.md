# Progress report - week 2
I implemented a radix tree that operates on bytes (or UTF-8 strings). It is
a vital component of parser, and allows me to move forward with it.
During testing, I uncovered multiple bugs in my implementation. As such, this
alone took a lot of time.

In addition, I worked a bit on basic (internal) APIs of Skripty. They are
nowhere close to being usable yet and are not yet tested well enough either.
CI and code coverage reporting are now online, and will hopefully show
improvement in the coming weeks.

Current priorities are getting Checkstyle configured and implementing more
of Skripty's core parts. A prototype parser is particularly critical. It is
so complex that I need to reserve time for a rewrite or two, if things don't
go as planned.