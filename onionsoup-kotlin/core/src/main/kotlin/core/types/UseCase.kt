package core.types

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.handleErrorWith
import arrow.mtl.EitherT
import org.slf4j.LoggerFactory

/**
 * A use case is just a function from a command <COMMAND> to an IO action
 * of type Either<ERROR,RESULT> where ERROR is some domain error (or unit) and RESULT
 * is the model update that should be applied.
 *
 * Results, errors, and commands are either simple data classes or sum types.
 * The return Type of a use case is IO<Either<ERROR, RESULT>> to allow side effects
 * (who needs a use case that does nothing but heat the cpu?). As IO actions can be
 * composed and evaluated at a later stage this also allows us to add logging, security,
 * or metrics at a later stage.
 */
typealias UseCaseLike<COMMAND, ERROR, RESULT> = (COMMAND) -> IO<Either<ERROR, RESULT>>
typealias LoadData<COMMAND, ERROR, DATA> = (COMMAND) -> IO<Either<ERROR, DATA>>
typealias Logic<COMMAND, DATA, ERROR, RESULT> = (COMMAND, DATA) -> Either<ERROR, RESULT>

typealias EitherIO<A, B> = EitherT<ForIO, A, B>


/**
 * Create a simple use case. Every use case consists of the following steps:
 * 1) load the data needed for the logic
 * 2) when data was loaded successfully (Right) call the logic with the loaded data and command.
 * 3) when logic was called successfully (Right) call persistModel with the result of the logic function
 *
 */
fun <COMMAND : Command, DATA, ERROR, RESULT> createUseCase(
  name: String,
  loadData: LoadData<COMMAND, ERROR, DATA>,
  logic: Logic<COMMAND, DATA, ERROR, RESULT>,
  persistModel: ModelUpdater<RESULT>
): UseCaseLike<COMMAND, ERROR, RESULT> =
  { command ->
    IO.fx {
      UseCases.LOG.info("[$name] [command: $command] - start")
      val data = !loadData(command)
      UseCases.LOG.info("[$name] [command: $command] - loaded data")
      val result = data.flatMap { logic(command, it) }
      UseCases.LOG.info("[$name] [command: $command] - invoked logic")
      val persistedResult = !result.fold(
        ifLeft = {
          UseCases.LOG.info("[$name] [command: $command] - update logic failed: $it")
          IO.just(result)
        },
        ifRight = { success ->
          persistModel(success)
            .map {
              UseCases.LOG.info("[$name] [command: $command] - model updated")
              result
            }
        }
      )
      persistedResult
    }.handleErrorWith { IO.raiseError(ApplicationException(it, command.requestId)) }
  }

internal object UseCases {
  internal val LOG = LoggerFactory.getLogger("requests")
}

abstract class UseCase<COMMAND : Command, ERROR, RESULT>(private val name: String) : UseCaseLike<COMMAND, ERROR, RESULT> {

  protected abstract fun execute(command: COMMAND) : IO<Either<ERROR, RESULT>>

  protected abstract fun persist(result: RESULT) : IO<Unit>

  override operator fun invoke(command: COMMAND) : IO<Either<ERROR, RESULT>> =
    IO.fx {
      UseCases.LOG.info("[$name] [command: $command] - start")
      val result = !execute(command)
      UseCases.LOG.info("[$name] [command: $command] - executed")
      !result.fold (
        ifLeft = {IO.unit},
        ifRight = ::persist
      )
      UseCases.LOG.info("[$name] [command: $command] - finished")
      result
    }.handleErrorWith {
      LOG.error("[$name] [command: $command] failed", it)
      IO.raiseError(ApplicationException(it, command.requestId))
    }

  companion object {
    private val LOG = LoggerFactory.getLogger(UseCase::class.java)
  }
}
