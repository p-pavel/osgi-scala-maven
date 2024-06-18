package com.perikov.cassandra.macros

class DispatchGeneration extends munit.FunSuite:
  test("no annotations") {
    val errs = compileErrors("""
      trait T:
        def f: Int = 42
      val a: T = new T {}
      val annotations = generateDispatch[T])(12, a)

    """)
    assert(
      errs.contains(
        "Expected exactly one annotation of type `dispatchBy`"
      )
    )
  }

  test("annotation with type") {
    class phony()            extends scala.annotation.StaticAnnotation
    class opcode(code: Byte) extends scala.annotation.StaticAnnotation

    @dispatchBy[opcode]
    trait T:
      @phony
      @opcode(10)
      def abc: Int = 42

      def cde: Float = 3.141f

    val t = new T {}

    assertEquals(generateDispatch(10, t), 42)
  }

  test("dispatch by name") {
    @dispatchByMethodName
    trait T:
      def abc: Int   = 42
      def cde: Float = 3.141f

    val t = new T {}

    assertEquals("abc".dispatcherByMethodName(t), 42)
    assertEquals("cde".dispatcherByMethodName(t), 3.141f)
  }

end DispatchGeneration
