package onionsoup.loanapplication.persistence

import arrow.core.getOrElse
import arrow.fx.IO
import arrow.fx.extensions.fx
import onionsoup.loanapplication.LoanApplicationCreated
import onionsoup.loanapplication.PersistLoanApplicationCreated
import onionsoup.loanapplication.persistence.sql.public_.Tables
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_OPERATOR
import org.jooq.Configuration
import org.jooq.DSLContext

internal fun createLoanApplication(dsl: DSLContext, event: LoanApplicationCreated): IO<Unit> = IO.fx {
  dsl.insertInto(
    Tables.LOAN_APPLICATION_ID,
    Tables.LOAN_APPLICATION_ID.ID,
    Tables.LOAN_APPLICATION_ID.CREATED_AT,
    Tables.LOAN_APPLICATION_ID.MODIFIED_AT
  ).values(
    event.aggregateId,
    event.occurredAt.toLocalDateTime(),
    event.occurredAt.toLocalDateTime()
  ).execute()

  dsl.insertInto(LOAN_APPLICATION_OPERATOR,
    LOAN_APPLICATION_OPERATOR.LOAN_APPLICATION_ID, LOAN_APPLICATION_OPERATOR.OPERATOR)
    .values(event.aggregateId, event.operatorHandle)
    .execute()

  // and persist validation errors. As persistErrors returns an io we can compose those nicely.
  !event.validationErrors
    .map { errors -> persistErrors(dsl, event.aggregateId, errors) }
    .getOrElse { IO.unit }
}

internal object JooqPersistLoanApplicationCreated {
  operator fun invoke(config: Configuration): PersistLoanApplicationCreated = JooqModelUpdateHandler(config, ::createLoanApplication)
}
