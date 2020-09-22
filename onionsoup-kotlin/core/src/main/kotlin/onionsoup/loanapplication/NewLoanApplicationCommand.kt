package onionsoup.loanapplication

import arrow.core.Either
import arrow.core.right
import core.types.Command
import onionsoup.loanapplication.LoanDraftValidator.validationErrors
import java.time.OffsetDateTime

data class NewLoanApplicationCommand(
  val loanApplicationId: String,
  val operatorHandle: String,
  override val requestId: String
) : Command

fun NewLoanApplicationCommand.execute(operator: Operator, now: OffsetDateTime): Either<NewLoanApplicationError, LoanApplicationCreated> {
  val draft = LoanApplicationDraft.newDraft(this.loanApplicationId, operatorHandle)
  val errors = draft.validationErrors()
  return LoanApplicationCreated(this.loanApplicationId, now, operatorHandle, errors).right()
}
