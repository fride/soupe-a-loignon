package onionsoup.loanapplication

import arrow.core.Either
import arrow.fx.IO
import core.extensions.mapSuccess
import core.extensions.utcNow
import core.types.UseCase
import java.time.Clock

class UpdateLoanApplicationUseCase(
  val findLoanDraftDraft: FindLoanApplicationDraft,
  val persistLoanApplicationDraftState: PersistLoanApplicationDraftChanged,
  val clock: Clock
) : UseCase<UpdateDraftCommand, UpdateDraftErrors, LoanApplicationDraftUpdated>("update loan application") {

  override fun execute(command: UpdateDraftCommand): IO<Either<UpdateDraftErrors, LoanApplicationDraftUpdated>> =
    findLoanDraftDraft(command.loanApplicationId)
      .map { it.toEither { UpdateDraftErrors.DraftNotFound } }
      .mapSuccess { loanApplicationDraft ->
        command.execute(loanApplicationDraft, clock.utcNow())
      }

  override fun persist(result: LoanApplicationDraftUpdated): IO<Unit> =
    persistLoanApplicationDraftState(result)
}
