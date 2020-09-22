package onionsoup.loanapplication

import arrow.core.Either
import arrow.core.Right
import arrow.fx.IO
import core.extensions.mapN
import core.extensions.today
import core.extensions.utcNow
import core.types.UseCase
import java.time.Clock
import java.time.LocalDate

class ScoreLoanApplicationUseCase(val findLoanApplication: FindLoanApplication,
                                  val clock: Clock,
                                  val persistLoanApplicationScored: PersistLoanApplicationScored,
                                  val getDebtorState: GetDebtorState
) : UseCase<ScoreLoanApplicationCommand, ScoreLoanApplicationError, LoanApplicationScored>("score loan application") {

  private fun createScoringRules(today: LocalDate, customer: Customer): IO<Either<ScoreLoanApplicationError, ScoringRule>> =
    getDebtorState(customer).map { debtorState ->
      Right(ScoringRules(debtorState = debtorState, today = today))
    }

  override fun execute(command: ScoreLoanApplicationCommand): IO<Either<ScoreLoanApplicationError, LoanApplicationScored>> =
    mapN(
      findLoanApplication(command.applicationId).map { it.toEither { ScoreLoanApplicationError.LoanApplicationNotFound } },
      { loanApplication -> createScoringRules(clock.today(), loanApplication.customer) }
    ) { loanApplication, scoringRule ->
      command(loanApplication, scoringRule, clock.utcNow())
    }

  override fun persist(result: LoanApplicationScored): IO<Unit> = persistLoanApplicationScored(result)
}
