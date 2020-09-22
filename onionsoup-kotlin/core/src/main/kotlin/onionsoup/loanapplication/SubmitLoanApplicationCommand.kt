package onionsoup.loanapplication

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import core.types.Command
import onionsoup.loanapplication.LoanDraftValidator.validate
import java.time.OffsetDateTime

data class SubmitLoanApplicationCommand(
  val loanApplicationDraftId: String,
  val operatorHandle: OperatorHandle,
  override val requestId: String
) : Command

// pure domain logic.
fun SubmitLoanApplicationCommand.execute(draft: LoanApplicationDraft, now: OffsetDateTime): Either<SubmitLoanApplicationError, LoanApplicationSubmitted> =
  draft.validate { id, customer, loan, property ->
    LoanApplicationSubmitted(
      aggregateId = id,
      property = property,
      customer = customer,
      loan = loan,
      operator = operatorHandle,
      occurredAt = now)
  }.toEither()
    .fold(
      { SubmitLoanApplicationError.DraftIsInvalid.left() },
      { loanApplication -> loanApplication.right() }
    )
