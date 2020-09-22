package core.extensions

import arrow.core.Tuple2
import arrow.fx.IO
import java.time.Duration

data class Timed(val name: String, val start: Duration)

fun <A> IO<A>.timed(name: String): IO<Tuple2<A, Timed>> =
  IO.just(System.nanoTime())
    .flatMap { start -> this.map { res -> res to start } }
    .map { x ->
      val timed = Timed(name, Duration.ofNanos(System.nanoTime() - x.second))
      Tuple2(x.first, timed)
    }
