package com.perikov.cassandra.macros

import scala.annotation.experimental

@experimental
class PrinterDeriving extends munit.FunSuite:
  trait Abc:
    def f1(g: Int, a: String): String
    def f2(t: Float, a: String): String
  end Abc

  test("deriving printing") {
    val a = derivePrinting[Abc]
    assertEquals(a.f1(10, "s"), "f1(10, s)")
    assertEquals(a.f2(0f, "s"), "f2(0.0, s)")
  }

  test("type aliasing") {
    type T = Abc
    val a: T = derivePrinting[T]
    assertEquals(a.f1(10, "s"), "f1(10, s)")
  }

  test("tagless final") {
    trait Q[T]:
      def f1(g: Int, a: String): T

    val a: Q[String] = derivePrinting[Q[String]]
  }

  test("type aliases") {
    type Q = Abc
    val t: Q = derivePrinting[Q]
    assertEquals(t.f1(10, "s"), "f1(10, s)")
  }

  test("type members") {
    trait Q: 
      type Self 
      def f(n: Int): Self

    trait S extends Q:
      type Self = String
    val a = derivePrinting[S]
    assertEquals(a.f(10), "f(10)")
  }

  test("abstract parameters and return types") {
    trait Abstract:
      type Self 
      type Arg1
      type Arg2
      def f(a1: Arg1, a2: Arg2): Self

    trait Speialized extends Abstract:
      type Self = String
      type Arg1 = Int
      type Arg2 = String

    val printer = derivePrinting[Speialized]
  }

  test("no arg methods") {
    trait T:
      def f: String



    val a = derivePrinting[T]
    assertEquals(a.f, "f")
  }
end PrinterDeriving
