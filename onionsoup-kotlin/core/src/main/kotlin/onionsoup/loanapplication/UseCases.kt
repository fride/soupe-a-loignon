package onionsoup.loanapplication

import core.extensions.utcNow
import core.types.UseCaseLike
import core.types.createUseCase
import java.time.Clock

typealias NewLoanApplicationUseCase = UseCaseLike<NewLoanApplicationCommand, NewLoanApplicationError, LoanApplicationCreated>
typealias SubmitLoanApplicationDraftUseCase = UseCaseLike<SubmitLoanApplicationCommand, SubmitLoanApplicationError, LoanApplicationSubmitted>

/**
 * Creates the `newLoanApplication` use case.
 */
fun newLoanApplication(clock: Clock,
                       findOperator: FindOperator,
                       modelUpdate: PersistLoanApplicationCreated): NewLoanApplicationUseCase =
  createUseCase(
    name = "new loan application draft",
    // could / should be using a side effect to generate a running number and an id for example
    loadData = { command ->
      findOperator(command.operatorHandle).map { it.toEither { NewLoanApplicationError.OperatorNotFound } }
    },
    logic = { command, operator -> command.execute(operator, clock.utcNow()) },
    persistModel = modelUpdate
  )


fun submitLoanApplicationDraft(
  clock: Clock,
  findLoanDraftDraft: FindLoanApplicationDraft,
  markDraftAsSubmitted: PersistLoanApplicationSubmitted
): SubmitLoanApplicationDraftUseCase = createUseCase(
  name = "submit loan application",
  loadData = { command -> findLoanDraftDraft(command.loanApplicationDraftId).map { it.toEither { SubmitLoanApplicationError.DraftNotFound } } },
  logic = { command, draft: LoanApplicationDraft -> command.execute(draft, clock.utcNow()) },
  persistModel = markDraftAsSubmitted
)

