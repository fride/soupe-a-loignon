package onionsoup.loanapplication.persistence

import arrow.fx.IO
import core.types.ModelUpdater
import org.jooq.Configuration
import org.jooq.DSLContext

fun Configuration.writeTransactionSync(f: (DSLContext) -> IO<Unit>): IO<Unit> =
  this.dsl().transactionResult { txConfig ->
    val dsl = txConfig.dsl()
    f(dsl)
  }

internal class JooqModelUpdateHandler<T>(
  private val config: Configuration,
  private val action: (DSLContext, T) -> IO<Unit>
) : ModelUpdater<T> {

  override operator fun invoke(event: T): IO<Unit> =
    config.writeTransactionSync { dsl -> action(dsl, event) }
}
