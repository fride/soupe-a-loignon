package onionsoup.loanapplication.persistence

import arrow.core.Either
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.typeclasses.ExitCase
import core.types.UseCaseLike
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import onionsoup.loanapplication.DebtorState
import onionsoup.loanapplication.FindLoanApplication
import onionsoup.loanapplication.FindLoanApplicationDraft
import onionsoup.loanapplication.FindOperator
import onionsoup.loanapplication.GetAllOperators
import onionsoup.loanapplication.GetOperator
import onionsoup.loanapplication.JooqPersistLoanApplicationScored
import onionsoup.loanapplication.LoanApplicationModule
import onionsoup.loanapplication.NewLoanApplicationUseCase
import onionsoup.loanapplication.PersistLoanApplicationCreated
import onionsoup.loanapplication.PersistLoanApplicationDraftChanged
import onionsoup.loanapplication.PersistLoanApplicationSubmitted
import onionsoup.loanapplication.ScoreLoanApplicationUseCase
import onionsoup.loanapplication.UpdateLoanApplicationUseCase
import onionsoup.loanapplication.newLoanApplication
import onionsoup.loanapplication.submitLoanApplicationDraft
import org.jooq.Configuration
import java.time.Clock

// look. DI without frameworks! ;)
class JooqLoanApplicationModule(configuration: Configuration, clock: Clock, meterRegistry: MeterRegistry) : LoanApplicationModule {

  private val persistLoanApplicationDraftChanged: PersistLoanApplicationDraftChanged = JooqPersistLoanApplicationDraftChanged(configuration)
  private val persistLoanApplicationCreated: PersistLoanApplicationCreated = JooqPersistLoanApplicationCreated(configuration)
  private val persistLoanApplicationSubmitted: PersistLoanApplicationSubmitted = JooqPersistLoanApplicationSubmitted(configuration)
  private val findLoanApplicationDraft: FindLoanApplicationDraft = JooqFindLoanApplicationDraft(configuration)
  private val findOperator: FindOperator = JooqFindOperator(configuration)
  private val findLoanApplication : FindLoanApplication = JooqFindLoanApplication(configuration)

  override val findLoanApplicationDetails = JooqFindLoanApplicationDetails(configuration).let { f ->
    { query: String -> f(query).timed(meterRegistry, "findLoanApplicationDetails") }
  }

  override val loadDashboard = jooqLoadDashboard(configuration)
  override val getOperator: GetOperator = JooqGetOperator(configuration)
  override val getAllOperators: GetAllOperators = JooqGetAllOperators(configuration)

  // an example of how to add metrics to use cases (or functions)
  override val newLoanApplicationUseCase: NewLoanApplicationUseCase = UseCaseWithMetrics(
    useCase = newLoanApplication(
      clock = clock,
      findOperator = findOperator,
      modelUpdate = persistLoanApplicationCreated),
    name = "newLoanApplication",
    meterRegistry = meterRegistry)

  override val updateLoanApplicationUseCase = UpdateLoanApplicationUseCase(
    findLoanDraftDraft = findLoanApplicationDraft,
    persistLoanApplicationDraftState = persistLoanApplicationDraftChanged,
    clock = clock)

  override val submitLoanApplicationDraftUseCase = submitLoanApplicationDraft(
    findLoanDraftDraft = findLoanApplicationDraft,
    markDraftAsSubmitted = persistLoanApplicationSubmitted,
    clock = clock)

  override val scoreLoanApplicationUseCase = ScoreLoanApplicationUseCase(
    findLoanApplication = findLoanApplication,
    clock= clock,
    persistLoanApplicationScored = JooqPersistLoanApplicationScored(configuration),
    getDebtorState = {IO.just(DebtorState.NotADebtor)}
  )

  override val createOperator = JooqCreateOperator(configuration)
  override val updateOperator = JooqUpdateOperator(configuration)
  override val deleteOperator = JooqDeleteOperator(configuration)
}


fun <A> IO<A>.timed(meterRegistry: MeterRegistry, name: String): IO<A> {
  val action = this
  return IO.fx {
    val sample = Timer.start(meterRegistry)
    val result = action
      .guaranteeCase {
        when (it) {
          is ExitCase.Completed -> sample.stop(meterRegistry.timer("name", "status", "success"))
          is ExitCase.Cancelled -> sample.stop(meterRegistry.timer("name", "status", "canceled"))
          is ExitCase.Error -> sample.stop(meterRegistry.timer("name", "status", "error"))
        }
        IO.unit
      }
      .bind()
    result
  }
}

private class UseCaseWithMetrics<COMMAND, ERROR, RESULT>(
  private val useCase: UseCaseLike<COMMAND, ERROR, RESULT>,
  private val name: String,
  private val meterRegistry: MeterRegistry
) : UseCaseLike<COMMAND, ERROR, RESULT> {

  override fun invoke(command: COMMAND): IO<Either<ERROR, RESULT>> =
    IO.fx {
      val sample = Timer.start(meterRegistry)
      !useCase(command).map { result ->
        when (result) {
          is Either.Left -> sample.stop(meterRegistry.timer("usecase.$name", "status", "failure"))
          is Either.Right -> sample.stop(meterRegistry.timer("usecase.$name", "status", "success"))
        }
        result
      }.onError { error ->
        sample.stop(meterRegistry.timer("usecase.$name", "status", "error"))
        IO.raiseError(error)
      }
    }
}
