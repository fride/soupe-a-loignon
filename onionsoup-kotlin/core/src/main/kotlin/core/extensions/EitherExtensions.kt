package core.extensions

import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import arrow.fx.IO


fun <A, B, C> Either<A, B>.mapIO(f: (B) -> IO<C>) : IO<Either<A,C>> =
  this.fold(
    ifLeft = { IO.just(it.left()) },
    ifRight = { it -> f(it).map { it.right() }}
  )

fun <A, B, C> Either<A, B>.flatmapIO(f: (B) -> IO<Either<A,C>>) : IO<Either<A,C>> =
  this.fold(
    ifLeft = { IO.just(it.left()) },
    ifRight = { it -> f(it)}
  )


fun <A, B> Either<A, B>.getOrRaiseIoError(e: (A) -> Throwable): IO<B> =
  this.fold(
    ifLeft = { IO.raiseError<B>(e(it)) },
    ifRight = { IO.just(it) }
  )

fun <A, B> IO<Either<A, B>>.getOrRaiseIoError(e: (A) -> Throwable): IO<B> =
  this.flatMap { res -> res.getOrRaiseIoError(e) }

fun <A> IO<Option<A>>.getOrRaiseIoError(e: () -> Throwable): IO<A> =
  this.flatMap { res -> res.fold(
    ifEmpty = { IO.raiseError<A>(e()) },
    ifSome = { IO.just(it) }
  ) }
