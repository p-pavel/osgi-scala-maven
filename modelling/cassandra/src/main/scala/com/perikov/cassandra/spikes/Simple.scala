import cats.instances.double
class Wrapper[T](val unwrap: T) extends AnyVal

class TFSelf[T <: { type Self }](val unwrap: (t: T) ?=> t.Self)

type TFDisjoint[A, B] = TFSelf[Disjoint[A, B]]

object TFDisjoint:
  import cats.Bifunctor
  given disjBifunctor: Bifunctor[TFDisjoint] with
    def bimap[A, B, C, D](
        fab: TFDisjoint[A, B]
    )(f: A => C, g: B => D): TFDisjoint[C, D] =
      TFSelf { (t: Disjoint[C, D]) ?=>
        given Disjoint[A, B] with
          type Self = t.Self
          def left(a: A)  = t.left(f(a))
          def right(b: B) = t.right(g(b))
        fab.unwrap
      }

extension [A](a: A)
  def injLeft[B]: TFDisjoint[A, B]  =
    TFSelf[Disjoint[A, B]]((t: Disjoint[A, B]) ?=> t.left(a))
  def injRight[B]: TFDisjoint[B, A] =
    TFSelf[Disjoint[B, A]]((t: Disjoint[B, A]) ?=> t.right(a))

extension [A, B](t: TFDisjoint[A, B])
  def toEither: Either[A, B] =
    given Disjoint[A, B] with
      type Self = Either[A, B]
      def left(a: A)  = Left(a)
      def right(b: B) = Right(b)
    t.unwrap

@main
def simple                   = 
  for i <- 1 to 10000 do
    print(s"$i:\t")
    val iter = 10_000_000
    val start = System.nanoTime()
    val t = (1 to iter).map(_.toLong.injRight).map(_.toEither).map(_.getOrElse(-1l)).sum
    val stop = System.nanoTime()
    val dur = (stop - start).toDouble/iter
    println((t,dur))
