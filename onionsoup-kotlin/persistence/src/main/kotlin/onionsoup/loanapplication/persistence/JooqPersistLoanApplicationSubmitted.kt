package onionsoup.loanapplication.persistence

import arrow.fx.IO
import arrow.fx.extensions.fx
import onionsoup.loanapplication.LoanApplicationSubmitted
import onionsoup.loanapplication.PersistLoanApplicationSubmitted
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_STATE
import org.jooq.Configuration
import org.jooq.DSLContext

internal fun markApplicationDraftAsSubmitted(dsl: DSLContext, event: LoanApplicationSubmitted): IO<Unit> = IO.fx {
  dsl.insertInto(LOAN_APPLICATION_STATE,
    LOAN_APPLICATION_STATE.LOAN_APPLICATION_ID,
    LOAN_APPLICATION_STATE.SUBMITTED_AT)
    .values(event.aggregateId,
      event.occurredAt.toLocalDateTime())
    .execute()
  Unit
}

internal object JooqPersistLoanApplicationSubmitted {
  operator fun invoke(config: Configuration): PersistLoanApplicationSubmitted = JooqModelUpdateHandler(config, ::markApplicationDraftAsSubmitted)
}
