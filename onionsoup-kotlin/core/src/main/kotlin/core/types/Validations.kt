package core.types

import arrow.core.Either
import arrow.core.Invalid
import arrow.core.Nel
import arrow.core.Option
import arrow.core.Valid
import arrow.core.Validated
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.nel
import java.util.Collections

data class ValidationMessages(private val messages: Map<String, List<String>>) {

  operator fun plus(other: ValidationMessages): ValidationMessages =
    ValidationMessages(messages + other.messages)

  operator fun get(key: String): List<String> =
    Option.fromNullable(messages[key])
      .getOrElse { emptyList() }

  val isEmpty: Boolean = messages.isEmpty()

  fun asMap(): Map<String, List<String>> = Collections.unmodifiableMap(this.messages)

  companion object {
    fun empty() = ValidationMessages(mutableMapOf())
  }
}

typealias ValidationCheck<E, A> = (A) -> Either<E, A>

fun <E, A> Validated<E, A>.check(check: ValidationCheck<E, A>) =
  this.withEither { it.flatMap(check) }

fun <T, E> T?.required(onMissing: () -> E): Validated<Nel<E>, T> =
  if (this == null) {
    Invalid(onMissing().nel())
  } else {
    Valid(this)
  }

fun <E> String?.notEmpty(onMissing: () -> E): Validated<Nel<E>, String> =
  if (this == null || this.isEmpty() || this.isBlank()) {
    Invalid(onMissing().nel())
  } else {
    Valid(this)
  }

fun <E, A> Validated<Nel<E>, A>.errors(): List<E> = when (this) {
  is Invalid -> this.e.all
  else -> emptyList()
}

fun <E, T : Comparable<T>> Validated<Nel<E>, T>.min(min: T, onInvalid: () -> E): Validated<Nel<E>, T> =
  when (this) {
    is Validated.Valid -> if (this.a < min) Invalid(onInvalid().nel()) else this
    is Validated.Invalid -> this
  }

fun <E, T : Comparable<T>> Validated<Nel<E>, T>.max(max: T, onInvalid: () -> E): Validated<Nel<E>, T> =
  when (this) {
    is Validated.Valid -> if (this.a > max) Invalid(onInvalid().nel()) else this
    is Validated.Invalid -> this
  }

// marker interface ;)
interface ValidationErrors
