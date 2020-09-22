# Guidelines 

When writing software I try to use the following guidelines. I apply them both to larger parts of the program or system and to the smallest parts. Those Guidelines are

- **Constraints** can set you free.
- **Composition** - make everything composable.
- **Code as data** -  and data as code
- **Simplicity** - not as in easy but as in simple like Rich said,.
- **Fast Feedback**
- **Try! There is no do.** - or don't be afraid to make mistakes..

Some of those ideas may sound strange. So let me explain them. 

**Constraints can set you free:** If you need examples from real life take a look at Mozarts work, at Haikus or at 
  railways or street. There are many constraints when driving a car, but those constraints actually enable you to drive 
  fast. Try to drive 100 km/h in a forrest!

**Composition:** Solutions should be easily composable as this allows you to build big things from many small things. 
  Think Lego (or functions)

**Code as Data:** Now this is not a new idea like many Lisp programmers can tell you. But code as data does not mean one
  has to use macros - the *interpreter pattern* works nicely too, and you can implement this pattern in most languages.
  Here it is used to update the model state in the db. Every use case emits one event, or model update, and the persistence
  code interprets it.

**Simplicity:** In order to be able to compose larger things from small things the small things must be understandable 
  and *not* composed. Think of functions or pure data. 
  See also Gall's law: 
  *<<A complex system that works is invariably found to have evolved from a simple system that worked. 
  A complex system designed from scratch never works and cannot be patched up to make it work. You have to start over with a working simple system.>>* [John Gall](https://en.wikipedia.org/wiki/John_Gall_(author)#Gall's_law)

**Fast Feedback and Try:** Those two make sense in combination. If your code allows you to try out things and get feedback fast you don't have to be afraid of huge refactor weekends and other nasty things. Also, if you get feedback fast you will be able to test your idea against reality.

In order to satisfy those constraints - and to show that (*as nice as Spring is*) one can write apps without it - I 
 decided to not use Spring and JPA but [Javalin](https://javalin.io) and [jOOQ](https://www.jooq.org). As a consequence
 
 * the domain code is free of framework code, no `@Entity` or `@Transactional` annotations anywhere.
 * the tests are simpler and faster.
 * the server starts in milliseconds.
 * there are no lazy loading bugs.
 * there are no bugs because of closed transactions.
 * tests can be written without mocks as most components are functions with in and output.
 * I had to write some more lines of code for persistence. ;)
 
