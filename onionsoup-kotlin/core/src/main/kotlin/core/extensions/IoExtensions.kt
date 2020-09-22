package core.extensions

import arrow.core.Either
import arrow.core.left
import arrow.fx.IO

// I don;t get this eitherT in Kotlin to run ;)
fun <ERR, A, B, C> mapN(
  init: IO<Either<ERR, A>>,
  next: (A) -> IO<Either<ERR, B>>,
  f: (A,B) -> Either<ERR, C>): IO<Either<ERR, C>> =
  init.flatMap { a ->
    a.fold(
      { IO.just(it.left()) },
      { sucessA -> next(sucessA).map { it.fold(
        {err -> err.left()},
        {successB -> f(sucessA, successB)}
      ) } }
    )
  }


fun <A, B, C> IO<Either<A, B>>.zipE(other: (B) -> IO<Either<A, C>>): IO<Either<A, Pair<B, C>>> =
  this.andThen { b -> other(b).map { it.map { c -> b to c } } }

fun <A, B, C> IO<Either<A, B>>.andThen(other: (B) -> IO<Either<A, C>>): IO<Either<A, C>> =
  this.flatMap { be ->
    be.fold(
      ifLeft = { err -> IO.just(err.left()) },
      ifRight = { b -> other(b) }
    )
  }

fun <A, B, C> IO<Either<A, B>>.mapSuccess(other: (B) -> Either<A, C>): IO<Either<A, C>> =
  this.flatMap { be ->
    be.fold(
      ifLeft = { err -> IO.just(err.left()) },
      ifRight = { b -> IO.just(other(b)) }
    )
  }
