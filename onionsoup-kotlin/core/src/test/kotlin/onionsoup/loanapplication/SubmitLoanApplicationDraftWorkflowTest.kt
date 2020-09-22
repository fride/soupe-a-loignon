package onionsoup.loanapplication

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Right
import arrow.core.orNull
import arrow.core.some
import arrow.fx.IO
import core.extensions.utcNow
import io.kotest.assertions.arrow.either.beLeft
import io.kotest.assertions.arrow.option.beNone
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import onionsoup.loanapplication.LoanDraftValidator.validateCustomer
import onionsoup.loanapplication.LoanDraftValidator.validatedLoan
import onionsoup.loanapplication.LoanDraftValidator.validatedProperty
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class SubmitLoanApplicationDraftWorkflowTest : BehaviorSpec({

  val clock: Clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
  var event: Option<LoanApplicationSubmitted> = None
  val completeLoanApplicationDraft = LoanApplicationDraftFixtures.completeLoanApplicationDraft
  given("an existing but invalid draft with id 0815") {
    val findLoanDraft: (String) -> IO<Option<LoanApplicationDraft>> = { _ -> IO.just(LoanApplicationDraft.newDraft("0815", "test").some()) }
    val markDraftAsSubmitted: (LoanApplicationSubmitted) -> IO<Unit> = { event = it.some(); IO.unit }
    val workflow = submitLoanApplicationDraft(
      findLoanDraftDraft = findLoanDraft,
      markDraftAsSubmitted = markDraftAsSubmitted,
      clock = clock)

    lateinit var result: Either<SubmitLoanApplicationError, LoanApplicationSubmitted>
    `when`("the draft is submitted") {
      result = workflow(SubmitLoanApplicationCommand(loanApplicationDraftId = "0815", operatorHandle = OperatorHandle("op"), requestId = "foo")).unsafeRunSync()

      then("An error with invalid data should be rurened") {
        result.should(beLeft())
      }
      then("no event shoud be emitted") {
        event.should(beNone())
      }
    }
  }
  given("an existing complete and valid draft with id 12") {
    val findLoanDraft: (String) -> IO<Option<LoanApplicationDraft>> = { id -> IO.just(completeLoanApplicationDraft.some()) }
    val markDraftAsSubmitted: (LoanApplicationSubmitted) -> IO<Unit> = { event = it.some(); IO.unit }

    val workflow = submitLoanApplicationDraft(
      findLoanDraftDraft = findLoanDraft,
      markDraftAsSubmitted = markDraftAsSubmitted,
      clock = clock)

    lateinit var result: Either<SubmitLoanApplicationError, LoanApplicationSubmitted>

    val expectedEvent = LoanApplicationSubmitted(
      occurredAt = clock.utcNow(),
      property = completeLoanApplicationDraft.property.validatedProperty().orNull()!!,
      loan = completeLoanApplicationDraft.loan.validatedLoan().orNull()!!,
      customer = completeLoanApplicationDraft.customer.validateCustomer().orNull()!!,
      aggregateId = "12",
      operator = OperatorHandle("op")
    )

    `when`("the draft is submitted") {

      result = workflow(SubmitLoanApplicationCommand(loanApplicationDraftId = "12", operatorHandle = OperatorHandle("op"), requestId = "foo")).unsafeRunSync()

      then("a loan application with state = submitted should be created") {
        result shouldBe Right(expectedEvent)
      }

      then("An event with the loan data shoud be emited") {
        event shouldBe (expectedEvent.some())
      }
    }
  }
})
