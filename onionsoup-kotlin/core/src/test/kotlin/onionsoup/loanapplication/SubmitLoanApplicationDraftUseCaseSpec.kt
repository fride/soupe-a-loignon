package onionsoup.loanapplication

import arrow.core.Option
import arrow.core.some
import arrow.fx.IO
import core.extensions.utcNow
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import onionsoup.loanapplication.LoanApplicationDraftFixtures.loanApplication
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class SubmitLoanApplicationDraftUseCaseSpec : FunSpec({
  val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
  context("SubmitLoanApplicationDraftUseCase") {
    test("submit invalid draft") {
      val useCase = submitLoanApplicationDraft(
        clock = clock,
        findLoanDraftDraft = { IO.just(LoanApplicationDraft.newDraft(it, "test2").some()) },
        markDraftAsSubmitted = LoanApplicationDraftFixtures.noOpModelUpdater()
      )
      useCase(SubmitLoanApplicationCommand("12", OperatorHandle("op"), "rid")).unsafeRunSync() shouldBeLeft SubmitLoanApplicationError.DraftIsInvalid
    }

    test("submit valid loan draft") {
      val useCase =
        submitLoanApplicationDraft(
          clock = clock,
          findLoanDraftDraft = {
            IO.just(LoanApplicationDraftFixtures.completeLoanApplicationDraft
              .copy(id = it)
              .some())
          },
          markDraftAsSubmitted = LoanApplicationDraftFixtures.noOpModelUpdater()
        )
      useCase(SubmitLoanApplicationCommand("12", OperatorHandle("op"),"rid")).unsafeRunSync() shouldBeRight LoanApplicationSubmitted(
        loan = LoanApplicationDraftFixtures.loanApplication.loan,
        occurredAt = clock.utcNow(),
        property = loanApplication.property,
        customer = loanApplication.customer,
        aggregateId = "12",
        operator = OperatorHandle("op")
      )
    }

    test("submit non existing  loan draft") {
      val useCase =
        submitLoanApplicationDraft(
          clock = clock,
          findLoanDraftDraft = { IO.just(Option.empty()) },
          markDraftAsSubmitted = LoanApplicationDraftFixtures.noOpModelUpdater()
        )
      useCase(SubmitLoanApplicationCommand("12", OperatorHandle("op"),"rid")).unsafeRunSync() shouldBeLeft SubmitLoanApplicationError.DraftNotFound
    }
  }
})
