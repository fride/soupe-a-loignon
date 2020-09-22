package onionsoup.loanapplication.persistence

import arrow.fx.IO
import onionsoup.loanapplication.Address
import onionsoup.loanapplication.City
import onionsoup.loanapplication.Country
import onionsoup.loanapplication.Customer
import onionsoup.loanapplication.FindLoanApplication
import onionsoup.loanapplication.Loan
import onionsoup.loanapplication.LoanApplication
import onionsoup.loanapplication.LoanApplicationState
import onionsoup.loanapplication.Name
import onionsoup.loanapplication.OperatorHandle
import onionsoup.loanapplication.Property
import onionsoup.loanapplication.Street
import onionsoup.loanapplication.ZipCode
import onionsoup.loanapplication.persistence.sql.public_.Tables
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_DETAILS
import org.jooq.Configuration
import org.jooq.Record
import java.time.ZoneOffset


fun Record.operatorHandle() : OperatorHandle = OperatorHandle( this[LOAN_APPLICATION_DETAILS.OPERATOR]!!)

fun Record.loanApplicationState() : LoanApplicationState =
  if (this[LOAN_APPLICATION_DETAILS.ACCEPTED_AT] != null) {
    LoanApplicationState.Accepted(
      submittedBy = this.operatorHandle(),
      submissionDate = this[LOAN_APPLICATION_DETAILS.ACCEPTED_AT].atOffset(ZoneOffset.UTC)
    )
  } else if (this[LOAN_APPLICATION_DETAILS.REJECTED_AT] != null) {
    LoanApplicationState.Rejected(
      submittedBy = this.operatorHandle(),
      submissionDate = this[LOAN_APPLICATION_DETAILS.ACCEPTED_AT].atOffset(ZoneOffset.UTC)
    )
  } else if (this[LOAN_APPLICATION_DETAILS.CHECKED_AT] != null) {
    TODO()
  } else if (this[LOAN_APPLICATION_DETAILS.SUBMITTED_AT] != null) {
    LoanApplicationState.Submitted(
      submittedBy = this.operatorHandle(),
      submissionDate = this[LOAN_APPLICATION_DETAILS.SUBMITTED_AT].atOffset(ZoneOffset.UTC)
    )
  } else {
    TODO()
  }

object JooqFindLoanApplication {
  operator fun invoke(configuration: Configuration): FindLoanApplication = { id ->
    configuration.readOnlyTransaction { dsl ->
      IO {
        dsl.selectLoanApplicationDetails()
          .from(LOAN_APPLICATION_DETAILS)
          .where(LOAN_APPLICATION_DETAILS.ID.eq(id))
          .and(LOAN_APPLICATION_DETAILS.SUBMITTED_AT.isNotNull)
          .fetchOptional()
          .map {
            LoanApplication(
              id = id,
              loan = Loan(
                amount = it[LOAN_APPLICATION_DETAILS.LOAN_AMOUNT].fromEuroCents()!!,
                interestRate = it[LOAN_APPLICATION_DETAILS.LOAN_INTEREST_RATE].fromEuroCents()!!,
                duration = it[LOAN_APPLICATION_DETAILS.LOAN_DURATION]
              ),
              property = Property(
                value = it[LOAN_APPLICATION_DETAILS.PROPERTY_VALUE].fromEuroCents()!!,
                address = Address(
                  street = Street(it[LOAN_APPLICATION_DETAILS.PROPERTY_STREET]),
                  zipCode = ZipCode(it[LOAN_APPLICATION_DETAILS.PROPERTY_ZIP_CODE]),
                  city = City(it[LOAN_APPLICATION_DETAILS.PROPERTY_CITY]),
                  country = Country(it[LOAN_APPLICATION_DETAILS.PROPERTY_CUNTRY])
                )
              ),
              customer = Customer(
                name = Name(it[LOAN_APPLICATION_DETAILS.CUSTOMER_FIRST_NAME],it[LOAN_APPLICATION_DETAILS.CUSTOMER_LAST_NAME]),
                monthlyIncome = it[LOAN_APPLICATION_DETAILS.CUSTOMER_MONTHLY_INCOME].fromEuroCents()!!,
                birthDate = it[LOAN_APPLICATION_DETAILS.CUSTOMER_BIRTH_DATE],
                address = Address(
                  street = Street(it[LOAN_APPLICATION_DETAILS.CUSTOMER_STREET]),
                  zipCode = ZipCode(it[LOAN_APPLICATION_DETAILS.CUSTOMER_ZIP_CODE]),
                  city = City(it[LOAN_APPLICATION_DETAILS.CUSTOMER_CITY]),
                  country = Country(it[LOAN_APPLICATION_DETAILS.CUSTOMER_COUNTRY])
                )
              ),
              state = it.loanApplicationState()
            )
          }.toOption()
      }
    }
  }
}
