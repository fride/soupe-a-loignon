package onionsoup.loanapplication.persistence

import arrow.core.Option
import arrow.fx.IO
import arrow.fx.extensions.fx
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import core.types.ValidationMessages
import onionsoup.loanapplication.Name
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_DETAILS
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_ID
import onionsoup.loanapplication.readmodel.CustomerDataDto
import onionsoup.loanapplication.readmodel.FindLoanApplicationDetails
import onionsoup.loanapplication.readmodel.LoanApplicationDetails
import onionsoup.loanapplication.readmodel.LoanDataDto
import onionsoup.loanapplication.readmodel.PropertyDataDto
import onionsoup.loanapplication.readmodel.ValidationMessagesDto
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.Record
import java.time.ZoneOffset

internal fun Record.validationErrors(): ValidationMessagesDto {
  return this[LOAN_APPLICATION_DETAILS.VALIDATION_ERRORS]?.let {
    val mapper = jacksonObjectMapper()
    mapper.readValue(it.data(), ValidationMessagesDto::class.java)
  } ?: ValidationMessagesDto(emptyMap(), emptyMap(), emptyMap())
}

internal fun findLoanApplicationDetails(dsl: DSLContext, id: String): IO<Option<LoanApplicationDetails>> = IO.fx {
  dsl.select(
    LOAN_APPLICATION_DETAILS.CUSTOMER_FIRST_NAME,
    LOAN_APPLICATION_DETAILS.CUSTOMER_LAST_NAME,
    LOAN_APPLICATION_DETAILS.CUSTOMER_BIRTH_DATE,
    LOAN_APPLICATION_DETAILS.CUSTOMER_MONTHLY_INCOME,
    LOAN_APPLICATION_DETAILS.CUSTOMER_BIRTH_DATE,
    LOAN_APPLICATION_DETAILS.CUSTOMER_STREET,
    LOAN_APPLICATION_DETAILS.CUSTOMER_ZIP_CODE,
    LOAN_APPLICATION_DETAILS.CUSTOMER_CITY,
    LOAN_APPLICATION_DETAILS.CUSTOMER_COUNTRY,

    LOAN_APPLICATION_DETAILS.LOAN_AMOUNT,
    LOAN_APPLICATION_DETAILS.LOAN_DURATION,
    LOAN_APPLICATION_DETAILS.LOAN_INTEREST_RATE,
    LOAN_APPLICATION_DETAILS.VALIDATION_ERRORS,

    LOAN_APPLICATION_DETAILS.PROPERTY_VALUE,
    LOAN_APPLICATION_DETAILS.PROPERTY_STREET,
    LOAN_APPLICATION_DETAILS.PROPERTY_ZIP_CODE,
    LOAN_APPLICATION_DETAILS.PROPERTY_CITY,
    LOAN_APPLICATION_DETAILS.PROPERTY_CUNTRY,

    LOAN_APPLICATION_DETAILS.SUBMITTED_AT,
    LOAN_APPLICATION_DETAILS.OPERATOR,
    LOAN_APPLICATION_DETAILS.OPERATOR_FIRST_NAME,
    LOAN_APPLICATION_DETAILS.OPERATOR_LAST_NAME,

    LOAN_APPLICATION_ID.MODIFIED_AT
  )
    .from(LOAN_APPLICATION_DETAILS)
    .leftOuterJoin(LOAN_APPLICATION_ID).on(LOAN_APPLICATION_ID.ID.eq(LOAN_APPLICATION_DETAILS.ID))
    .where(LOAN_APPLICATION_DETAILS.ID.eq(id))
    .fetchOptional()
    .map {
      val errors = it.validationErrors()
      LoanApplicationDetails(
        loanApplicationId = id,
        operatorHandle = it[LOAN_APPLICATION_DETAILS.OPERATOR],
        operatorName = Name(it[LOAN_APPLICATION_DETAILS.OPERATOR_FIRST_NAME], it[LOAN_APPLICATION_DETAILS.OPERATOR_LAST_NAME]),
        changedAt = it[LOAN_APPLICATION_ID.MODIFIED_AT].atOffset(ZoneOffset.UTC),
        state = if (it[LOAN_APPLICATION_DETAILS.SUBMITTED_AT] == null) "draft" else "final",
        loan = LoanDataDto(
          amount = it[LOAN_APPLICATION_DETAILS.LOAN_AMOUNT].fromEuroCents(),
          duration = it[LOAN_APPLICATION_DETAILS.LOAN_DURATION],
          interestRate = it[LOAN_APPLICATION_DETAILS.LOAN_INTEREST_RATE].fromEuroCents(),
          _messages = ValidationMessages(errors.loan)
        ),
        property = PropertyDataDto(
          value = it[LOAN_APPLICATION_DETAILS.PROPERTY_VALUE].fromEuroCents(),
          propertyStreet = it[LOAN_APPLICATION_DETAILS.PROPERTY_STREET],
          propertyZipCode = it[LOAN_APPLICATION_DETAILS.PROPERTY_ZIP_CODE],
          propertyCity = it[LOAN_APPLICATION_DETAILS.PROPERTY_CITY],
          propertyCountry = it[LOAN_APPLICATION_DETAILS.PROPERTY_CUNTRY],
          _messages = ValidationMessages(errors.property)
        ),
        customer = CustomerDataDto(
          customerFirstName = it[LOAN_APPLICATION_DETAILS.CUSTOMER_FIRST_NAME],
          customerLastName = it[LOAN_APPLICATION_DETAILS.CUSTOMER_LAST_NAME],
          customerBirthDate = it[LOAN_APPLICATION_DETAILS.CUSTOMER_BIRTH_DATE],
          customerMonthlyIncome = it[LOAN_APPLICATION_DETAILS.CUSTOMER_MONTHLY_INCOME].fromEuroCents(),
          customerStreet = it[LOAN_APPLICATION_DETAILS.CUSTOMER_STREET],
          customerCity = it[LOAN_APPLICATION_DETAILS.CUSTOMER_CITY],
          customerZipCode = it[LOAN_APPLICATION_DETAILS.CUSTOMER_ZIP_CODE],
          customerCountry = it[LOAN_APPLICATION_DETAILS.CUSTOMER_COUNTRY],
          _messages = ValidationMessages(errors.customer)
        )
      )
    }.toOption()
}

internal object JooqFindLoanApplicationDetails {
  operator fun invoke(configuration: Configuration): FindLoanApplicationDetails = { id ->
    configuration.readOnlyTransaction { dsl -> findLoanApplicationDetails(dsl, id) }
  }
}
