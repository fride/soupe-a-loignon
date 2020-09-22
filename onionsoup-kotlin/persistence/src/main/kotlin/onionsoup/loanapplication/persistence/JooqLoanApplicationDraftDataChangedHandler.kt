package onionsoup.loanapplication.persistence

import arrow.fx.IO
import arrow.fx.extensions.fx
import onionsoup.loanapplication.LoanApplicationDraftUpdated.LoanApplicationDraftChange
import onionsoup.loanapplication.LoanApplicationDraftUpdated
import onionsoup.loanapplication.PersistLoanApplicationDraftChanged
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_CUSTOMER
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_ID
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_LOAN
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_PROPERTY
import org.jooq.Configuration
import org.jooq.DSLContext

internal fun writePropertyData(aggregateId: String, event: LoanApplicationDraftChange.PropertyDataChanged, dsl: DSLContext): IO<Unit> = IO.fx {
  dsl.delete(LOAN_APPLICATION_PROPERTY).where(LOAN_APPLICATION_PROPERTY.LOAN_APPLICATION_ID.eq(aggregateId)).execute() // because I'm to lazy to update
  dsl.insertInto(
    LOAN_APPLICATION_PROPERTY,
    LOAN_APPLICATION_PROPERTY.LOAN_APPLICATION_ID,
    LOAN_APPLICATION_PROPERTY.VALUE,
    LOAN_APPLICATION_PROPERTY.STREET,
    LOAN_APPLICATION_PROPERTY.ZIP_CODE,
    LOAN_APPLICATION_PROPERTY.CITY,
    LOAN_APPLICATION_PROPERTY.COUNTRY)
    .values(
      aggregateId,
      event.propertyData.value.asEuroCents(),
      event.propertyData.address.street,
      event.propertyData.address.zipCode,
      event.propertyData.address.city,
      event.propertyData.address.country
    ).execute()
  Unit
}

internal fun writeLoanData(aggregateId: String, event: LoanApplicationDraftChange.LoanDataChanged, dsl: DSLContext): IO<Unit> = IO.fx {
  dsl.delete(LOAN_APPLICATION_LOAN).where(LOAN_APPLICATION_LOAN.LOAN_APPLICATION_ID.eq(aggregateId)).execute() // because I'm to lazy to update
  dsl.insertInto(
    LOAN_APPLICATION_LOAN,
    LOAN_APPLICATION_LOAN.LOAN_APPLICATION_ID,
    LOAN_APPLICATION_LOAN.AMOUNT,
    LOAN_APPLICATION_LOAN.DURATION,
    LOAN_APPLICATION_LOAN.INTEREST_RATE)
    .values(
      aggregateId,
      event.loanData.amount.asEuroCents(),
      event.loanData.duration,
      event.loanData.interestRate.asEuroCents()) // it's a lie. it is percent!
    .execute()
  Unit
}

internal fun writeCustomerData(aggregateId: String, event: LoanApplicationDraftChange.CustomerDataChanged, dsl: DSLContext): IO<Unit> = IO.fx {
  dsl.delete(LOAN_APPLICATION_CUSTOMER).where(LOAN_APPLICATION_CUSTOMER.LOAN_APPLICATION_ID.eq(aggregateId)).execute() // because I'm to lazy to update
  dsl.insertInto(
    LOAN_APPLICATION_CUSTOMER,
    LOAN_APPLICATION_CUSTOMER.LOAN_APPLICATION_ID,
    LOAN_APPLICATION_CUSTOMER.FIRST_NAME,
    LOAN_APPLICATION_CUSTOMER.SECOND_NAME,
    LOAN_APPLICATION_CUSTOMER.BIRTH_DATE,
    LOAN_APPLICATION_CUSTOMER.MONTHLY_INCOME,
    LOAN_APPLICATION_CUSTOMER.STREET,
    LOAN_APPLICATION_CUSTOMER.ZIP_CODE,
    LOAN_APPLICATION_CUSTOMER.COUNTRY,
    LOAN_APPLICATION_CUSTOMER.CITY
  ).values(
    aggregateId,
    event.customerData.firstName,
    event.customerData.lastName,
    event.customerData.birthDate,
    event.customerData.monthlyIncome.asEuroCents(),
    event.customerData.address.street,
    event.customerData.address.zipCode,
    event.customerData.address.country,
    event.customerData.address.city)
    .execute()
  Unit
}

internal fun writeUpdateData(event: LoanApplicationDraftUpdated, dsl: DSLContext): IO<Unit> = IO.fx {
  dsl.update(LOAN_APPLICATION_ID)
    .set(LOAN_APPLICATION_ID.MODIFIED_AT, event.occurredAt.toLocalDateTime())
    .where(LOAN_APPLICATION_ID.ID.eq(event.aggregateId))
    .execute()
  Unit
}

private fun handleChange(aggregateId: String, loanApplicationDraftChange: LoanApplicationDraftChange, dsl: DSLContext): IO<Unit> =
  when (loanApplicationDraftChange) {
    is LoanApplicationDraftChange.CustomerDataChanged -> writeCustomerData(aggregateId, loanApplicationDraftChange, dsl)
    is LoanApplicationDraftChange.LoanDataChanged -> writeLoanData(aggregateId, loanApplicationDraftChange, dsl)
    is LoanApplicationDraftChange.PropertyDataChanged -> writePropertyData(aggregateId, loanApplicationDraftChange, dsl)
    is LoanApplicationDraftChange.DraftBecameInvalid -> persistErrors(dsl, aggregateId, loanApplicationDraftChange.errors)
    LoanApplicationDraftChange.DraftBecameValid -> removeErrors(dsl, aggregateId)
  }

internal fun updateLoanApplicationDraft(dsl: DSLContext, event: LoanApplicationDraftUpdated) = IO.fx {
  !writeUpdateData(event, dsl)
  for (change in event.changes.all) {
    !handleChange(event.aggregateId, change, dsl)
  }
}

internal object JooqPersistLoanApplicationDraftChanged {
  operator fun invoke(config: Configuration): PersistLoanApplicationDraftChanged = JooqModelUpdateHandler(config, ::updateLoanApplicationDraft)
}
