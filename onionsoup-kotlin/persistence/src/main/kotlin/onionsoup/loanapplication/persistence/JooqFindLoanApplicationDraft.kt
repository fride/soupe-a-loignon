package onionsoup.loanapplication.persistence

import arrow.core.Option
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.syntax.function.partially1
import onionsoup.loanapplication.AddressDraft
import onionsoup.loanapplication.CustomerDraft
import onionsoup.loanapplication.FindLoanApplicationDraft
import onionsoup.loanapplication.LoanApplicationDraft
import onionsoup.loanapplication.LoanDraft
import onionsoup.loanapplication.PropertyDraft
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_DETAILS
import org.jooq.Configuration

internal fun findLoanApplication(configuration: Configuration, id: String): IO<Option<LoanApplicationDraft>> = IO.fx {
  configuration.dsl()
    .selectDraftDetails()
    .from(LOAN_APPLICATION_DETAILS)
    .where(LOAN_APPLICATION_DETAILS.ID.eq(id))
    .and(LOAN_APPLICATION_DETAILS.SUBMITTED_AT.isNull)
    .fetchOptional()
    .map {
      LoanApplicationDraft(
        id = id,
        loan = LoanDraft(
          amount = it[LOAN_APPLICATION_DETAILS.LOAN_AMOUNT].fromEuroCents(),
          interestRate = it[LOAN_APPLICATION_DETAILS.LOAN_INTEREST_RATE].fromEuroCents(),
          duration = it[LOAN_APPLICATION_DETAILS.LOAN_DURATION]
        ),
        property = PropertyDraft(
          value = it[LOAN_APPLICATION_DETAILS.PROPERTY_VALUE].fromEuroCents(),
          address = AddressDraft(
            street = it[LOAN_APPLICATION_DETAILS.PROPERTY_STREET],
            zipCode = it[LOAN_APPLICATION_DETAILS.PROPERTY_ZIP_CODE],
            city = it[LOAN_APPLICATION_DETAILS.PROPERTY_CITY],
            country = it[LOAN_APPLICATION_DETAILS.PROPERTY_CUNTRY]
          )
        ),
        customer = CustomerDraft(
          firstName = it[LOAN_APPLICATION_DETAILS.CUSTOMER_FIRST_NAME],
          lastName = it[LOAN_APPLICATION_DETAILS.CUSTOMER_LAST_NAME],
          monthlyIncome = it[LOAN_APPLICATION_DETAILS.CUSTOMER_MONTHLY_INCOME].fromEuroCents(),
          birthDate = it[LOAN_APPLICATION_DETAILS.CUSTOMER_BIRTH_DATE],
          address = AddressDraft(
            street = it[LOAN_APPLICATION_DETAILS.CUSTOMER_STREET],
            zipCode = it[LOAN_APPLICATION_DETAILS.CUSTOMER_ZIP_CODE],
            city = it[LOAN_APPLICATION_DETAILS.CUSTOMER_CITY],
            country = it[LOAN_APPLICATION_DETAILS.CUSTOMER_COUNTRY]
          )
        ),
        operator = it[LOAN_APPLICATION_DETAILS.OPERATOR] ?: "ERROR"
      )
    }.toOption()
}

internal object JooqFindLoanApplicationDraft {

  operator fun invoke(configuration: Configuration): FindLoanApplicationDraft = ::findLoanApplication.partially1(configuration)
}
