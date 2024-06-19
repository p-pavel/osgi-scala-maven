# The things I worked on and dreamed of

Unfortunately, I probably won't be able to continue any research at all, so here's something for you to glance through if you're interested.

I tend to think too much and not share anything half finished, so not much is published and now is not the appropriate time to try to pick the pieces and put them together.

In the last couple of years, I dreamed of using the potential of Scala and the potential of OSGi and binary artifact management.

We actually have incredible mature technologies laying in front of us, but their usage as I envision is far from common.

## On Scala

Scala 3 is an incredible language. Actually, I see no reason to write on anything else in
most of the cases.

What seems to be missed by general shops is the potential of traits and a variation on the tagless-final theme.

Please remember: Scala is an unparalleled *specification language*.

### Scala traits

Scala traits allow us to create the complete pocket universe or algebraic signature, focusing on the part of the problem.

The nature of traits, combined with `export` from values in traits and `extends` mechanics, lets us later mix things in any possible way.

You start thinking about the problem with some trait, describing the types and operations, establishing a multi-sort algebraic signature:

```scala
trait MavenResolver:
  type Artifact
  type Dependency <: Artifact
  type F[_] // our effect type
  extension (a: Artifact)
    def fetchDependencies: F[Set[Dependency]]
  ///...
```

This immediately gives us a clean definition of the problem at hand. Keep them small and combine later.
I have few techniques I developed, but alas.

The specification is ready to be extended or refined. It's mechanically checked by the compiler and is ready to be implemented.

Side note: offering this way of thinking during an interview when tasked with "design the API" gave me the label of "tends to overengineer," so you were warned here.

We can go a long way mixing specifications like this together using both inheritance and composition and types will play well 
due to DOT and path-dependent types nature in Scala.

We can provide zero-runtime-cost version of refined types based on primitives if we prefer.

Generally: specify types and specify operations on them, using `extension` freely to give
the feeling of "OO" dot-style code. `using` will often transparently bring things into scope.
Don't put the operation "*into*" types (assuming the type is `AnyRef`)

With Scala, we can be arbitrarily precise about types also and can specify both upper and lower bounds (take a look at "Maven coordinate space" in this project).

Using very precise types brings not only the benefits of compact and formally verified specification and documentation.

It gives you the ability to use *term deriving* -- another feature of the language. If your types are specified
with enough precision -- let compiler do the job. You need to specify rules of your world
with `given` of course. Most of the logic can be written with givens. 

Give the rules, not commands.

Solving the task starts with preparing good specs and good contexts, bringing types and rules (`given`) in scope.

### Tagless final and problem space analysis with scala traits and metaprogramming

Modelling the world with programming languages boils down to defining types and subtypes,
but also to proper usage of these main concepts:

- disjoint union types
- cartesian product types
- recursion schemes (see ["functional programming with bananas"](https://ris.utwente.nl/ws/portalfiles/portal/6142049/meijer91functional.pdf) )

for former two you don't need `enum`'s or `case class`es that populate your classpath with tons of trash,
you don't need no fucking `protobuf`s, nothing like this.

cartesian product types are extremely well modelled by... methods, their arguments forming the components of the product,
and disjoint union types are methods in the trait.

Pleass take a look at [Oleg Kiselev work on tagless final interpreterers](https://okmij.org/ftp/tagless-final/index.html)

I had also put some files here to illustrate my point (sorry, don't have time to make it compile here) for modelling
Cassandra native protocol (see "modelling" folder).

Using arbitrary annotations and bits of metaprogramming (also in modelling folder) you can extract
all kinds of useful code from your models.

You can also generate `protobuf` and whatever is in fashion in this season as you need.

Use Scala for your specifications.

The complete event-sourcing architecture, for example, can be specified with scala traits to serve
readily for documentation ("what it is?"). Extend the specs and get your implementation.

## On OSGi

Please note: OSGi is not some ancient technology from J2EE ages that is not worth knowing about.

It's JVM extension done right.

The pitiful attempts to use JVM as a runtime library make me feel sorrow.

OSGi is a tool to manage your classpath at runtime.

[OSGi specs](https://docs.osgi.org/specification/) are software engineering gems.

OSGi went 25 years without breaking binary compatibility.

There're lots of implementation, [Apache Karaf](https://karaf.apache.org/manual/latest/) being
what I use.

You get logging, shell you can ssh to, easy way to write commands for this shell, configuration management,
dependency injection, resolution and constraint solving,
deploying `wars`, `blueprint` or `spring` containers, interactive investigation what happens in your system
etc etc for free. Just put a couple of lines into your build and be happy.

```sbt
    OsgiKeys.privatePackage                         := Seq.empty,
    OsgiKeys.exportPackage                          := Seq(
      "com.perikov.maven.abstractions.*"
    ),
    OsgiKeys.importPackage                          := Seq(
      "*"
    )
```

Isn't this TOO HARD and TOO MUCH WORK?

It's old and alive and perfect. It has next to zero overhead. It's modularisation on JVM done right from the beginning.

We, Scalers, could leverage it's potential for using scala-2.12, 2.13, and 3 alongside in single JVM.

OSGi gives you hot code reloading, eviction and much, much, much more.

Don't port your 2.12 code. provide Java interfaces and use it from 3.

Unfortunately Scala community had ignored OSGi bundle specs that cost you as developer virtually nothing.

Specify what packages you export, and `bnd` tool plugin will do the rest.

Repeat after me: *JVM is not a runtime library. It's a virtual machine*. You don't need micro-jvms for
every single function containerised in kubernetes communicating over the virtual network using 
some crappy REST.

It's bullshit. You need proper designed interfaces in YOUR LANGUAGE running in long-living
JVM that never stops. Let JVM do it's magic with optimisation and let OSGi manage boundaries.

### OSGi and Scala

Unfortunately, many-many-many Scala libraries are perfect examples of bad software engineering
practice.

Interfaces and implementaion mixed together, "companion object" thing promoting this to extremes.

Arbitrary splitting into jars.

No OSGi information in jars.

And worst of all: package splitting.

I envision a tool (parts of it in this project) that given a maven coordinates of the `jar` produces
`Karaf` feature repo file (that can include references for other repo files) and describes bundles
using `wrap:` handler (specifying missing info in URL).

This seems completely doable.

We can have hand-written hints for libraries with ~~brain~~package-split problems using `Fragment-Host` idea.
Choose one of the `jar`s as fragment host and inject others into its classpath.

I think this tool can be easily written in a month.

We can extract the dependency information from
`sbt` project and feed it to the tool getting nice [`Karaf` features](https://karaf.apache.org/manual/latest/#_feature_and_resolver)
with full life cycle as the first
approximation for the existing code in ecosystem. I once wrote [some scripts](https://github.com/p-pavel/osgi-scala) for something similair.

And please don't put classes from different `jars` into the same package. Please. Don't.

BTW the situation with Scala 3 standard library is very interesting as it adds classes to scala-2.13 library.

### Binary artifact management

Many organisations I worked  for lack the proper binary artifact management, or didn't use it to full potential.
So we see 2500+ lines sbt files, rely on source code management in the project and get a complete mess with
circular dependencies, split-packages etc.

At some point of time put a little engineering effort into your project, split it into proper bundles and features.

It's not that time-consuming, but you will do a good software engineering.

*Package* is the main unit of reuse and versioning on JVM.

Have a small page on your internal project wiki listing the packages your organisation use and for what purpose.

That's really simple. And you get much better code.

### The development enviroment I dream of

Think about Maven Central. It contains ~650 000 of binary artifacts. All with specified
dependencies, `pom` files, readily downloadable java docs, sources etc etc.

I think the good IDE will provide the left bar populated with packages (not the filesystem directories) and
should primary be focused on working with packages that you can just chose from Maven.

Forget specifying dependencies (this is a bad thing after all -- you have to separate what you depend
on and what implementation is used in the system. The latter is deployment thing that should be
performed by OSGi container).

You should work in an the world-wide space of packages.

The tool can intergrate with the OSGi container giving you instant compilation and code reloading
and enforce proper package organisation.

It should readily handle artifact publishing to maven central or corporative repo.

I think it should give you an illusion of Smalltalk-like live environment.

All the technologies are here, just assemble them and try to push this IDE into people's mind.

Good luck with the latter.

## On microservices

We live in the age of microservice catastrophe.

Everyone think they are Google and everyone think they should split their application
into microservices (read: docker containers containing a JVM with 1.5 vCPU available to it communicating
via REST, gRPC, whatever).

Why on Earth?

We have interfaces in Java/JVM (java is not a language, it's the platform ABI).

Design your application around interfaces, deploy to OSGi containers (see how Karaf does clustering using
Cellar to distribute configurations), let the parts talk via interfaces. Let JVM happily do
cross-service optimisation and inlining. The call through interace is in nanosecond range, the call
over gRPC is in milliseconds.

Why spend time specifying the API using protobuf or REST inside YOUR APPLICATION?

Just do it on the boundaries on the system.

Most of the time there's no reason to network RPC at all.

What will happen on the other end of the call what you can't do locally? Talking to Cassandra or Postgress?

You can do it from the service running locally.

Never do stupid things intentionally.

### Few words on Akka/Pecco

Don't use it. Akka will corrupt your codebase and push you to do all kind of stupid things.

You don't need stateful actors. You need async framework like cats-effect, or ZIO and distributed data
structures (queues, maps, sets etc).

Latter are provided with tools like SQL databases, beutiful Cassandra or in memory cluster Hazelcast.

You will not do any better.

Actor model is evil. It doesn't belong to the modern world. Actors do not compose.

Akka's "workshops" should be teached to the software engineers as the part of the course
"How to not do software engineering. NEVER".

Just take a look at this: https://doc.akka.io/guide/microservices-tutorial/overview.html

## Final words

Sorry, that was short, unstructured and full of mistakes, but I ran out of cigarettes.

I spent lots of time on things that are not proven, considered not worth doing, trying to
convince people that I should leave alone and not behaving good.

One of my most prominent mistakes was not keeping "the journal of negative results" -- the 
things I tried, spent weeks on and finally consider "not working".

But there's no sense crying over every mistake, you just keep on trying till you run out
of cake :)

If there're some ideas you like -- that's great. If all of the above is just useless rant -- that's
also perfect.

Have a nice software engineering and let Scala be with you.

https://www.youtube.com/watch?v=Y6ljFaKRTrI

