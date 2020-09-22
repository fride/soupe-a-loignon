package onionsoup.loanapplication.persistence

import arrow.fx.IO
import arrow.fx.extensions.fx
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import onionsoup.loanapplication.LoanApplicationValidationErrors
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_VALIDATION_ERRORS
import onionsoup.loanapplication.readmodel.toValidationMessagesDto
import org.jooq.DSLContext
import org.jooq.JSONB

internal fun removeErrors(dsl: DSLContext, aggregateId: String): IO<Unit> = IO.fx {
  dsl.deleteFrom(LOAN_APPLICATION_VALIDATION_ERRORS)
    .where(LOAN_APPLICATION_VALIDATION_ERRORS.LOAN_APPLICATION_ID
      .eq(aggregateId)).execute()
  Unit
}

internal fun persistErrors(dsl: DSLContext, aggregateId: String, errors: LoanApplicationValidationErrors): IO<Unit> = IO.fx {

  val messages = errors.toValidationMessagesDto()
  val mapper = jacksonObjectMapper()
  val json = mapper.writeValueAsString(messages)

  dsl.insertInto(LOAN_APPLICATION_VALIDATION_ERRORS,
    LOAN_APPLICATION_VALIDATION_ERRORS.LOAN_APPLICATION_ID,
    LOAN_APPLICATION_VALIDATION_ERRORS.ERRORS)
    .values(aggregateId, JSONB.valueOf(json.toString()))
    .onConflict()
    .doUpdate()
    .set(LOAN_APPLICATION_VALIDATION_ERRORS.ERRORS, JSONB.valueOf(json.toString()))
    .execute()
  Unit
}
