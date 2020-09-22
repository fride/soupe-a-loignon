package onionsoup.loanapplication

import arrow.core.Option
import arrow.core.orNull
import arrow.core.some
import arrow.fx.IO
import core.extensions.utcNow
import core.types.ApplicationException
import io.kotest.assertions.arrow.option.beSome
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import java.io.IOException
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * A simple unit test!
 */
class NewLoanApplicationUseCaseSpec : FunSpec({

  var operator : Option<Operator> = Operator(operatorHandle = "test", competenceLevel = BigDecimal("20000"), name = Name("Smooth", "Operator")).some()
  val findOperator : FindOperator = { IO.just(operator) }

  test("newLoanApplicationUseCase - happy case") {
    val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
    val modelUpdate = newLoanApplication(
      clock,
      findOperator = findOperator,
      modelUpdate = { event ->
        IO {
          event.aggregateId shouldBe "0815"
          event.occurredAt shouldBe clock.utcNow()
          event.validationErrors should beSome()
        }
      }
    )(NewLoanApplicationCommand(loanApplicationId = "0815", requestId = "12", operatorHandle = "test")).unsafeRunSync().orNull()!!
    modelUpdate.aggregateId shouldBe "0815"
    modelUpdate.occurredAt shouldBe clock.utcNow()
    modelUpdate.validationErrors should beSome()
  }

  test("newLoanApplicationUseCase - kabum!") {
    val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
    val useCase = newLoanApplication(
      clock,
      findOperator = findOperator,
      modelUpdate = { _ ->
        IO.raiseError(IOException("BOOM!"))
      }
    )(NewLoanApplicationCommand(loanApplicationId = "0815", requestId = "12", operatorHandle = "test"))
    val expectedException = shouldThrow<ApplicationException> { useCase.unsafeRunSync() }
    expectedException.id shouldBe "12" // the request id!
  }
})
