package onionsoup.loanapplication

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import core.types.Command
import java.time.OffsetDateTime

data class ScoreLoanApplicationCommand(
  val applicationId: String,
  override val requestId: String
) : Command {

  operator fun invoke(loanApplication: LoanApplication,
                      scoringRule: ScoringRule,
                      now: OffsetDateTime): Either<ScoreLoanApplicationError, LoanApplicationScored> =
    when (loanApplication.state) {
      is LoanApplicationState.Submitted -> {
        val score = scoringRule(loanApplication)
        LoanApplicationScored(
          aggregateId = loanApplication.id,
          occurredAt = now,
          score = score
        ).right()
      }
      is LoanApplicationState.Scored -> ScoreLoanApplicationError.LoanApplicationAlreadyScored.left()
      is LoanApplicationState.Accepted -> ScoreLoanApplicationError.LoanApplicationAlreadyAccepted.left()
      is LoanApplicationState.Rejected -> ScoreLoanApplicationError.LoanApplicationAlreadyRejected.left()
    }
}

