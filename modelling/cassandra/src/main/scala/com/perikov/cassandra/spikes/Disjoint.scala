trait Disjoint[-A,-B]:
  type Self
  def left(a: A): Self
  def right(b: B): Self