package onionsoup.loanapplication

import arrow.core.Either
import arrow.core.Nel
import arrow.core.left
import arrow.core.right
import core.types.Command
import onionsoup.loanapplication.LoanDraftValidator.validationErrors
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

sealed class UpdateDraftCommand : Command {

  abstract val loanApplicationId: String

  data class UpdateCustomerDataCommand(
          override val loanApplicationId: String,
          val customerFirstName: String?,
          val customerLastName: String?,
          val customerBirthDate: LocalDate?,
          val customerMonthlyIncome: BigDecimal?,
          val customerStreet: String?,
          val customerZipCode: String?,
          val customerCity: String?,
          val customerCountry: String?,
          override val requestId: String
  ) : UpdateDraftCommand()

  data class UpdateLoanDataCommand(
          override val loanApplicationId: String,
          val amount: BigDecimal?,
          val duration: Int?,
          val interestRate: BigDecimal?,
          override val requestId: String
  ) : UpdateDraftCommand()

  data class UpdatePropertyDataCommand(
          override val loanApplicationId: String,
          val value: BigDecimal?,
          val propertyStreet: String?,
          val propertyZipCode: String?,
          val propertyCity: String?,
          val propertyCountry: String?,
          override val requestId: String
  ) : UpdateDraftCommand()
}
// pure domain logic without domain events
fun UpdateDraftCommand.execute(draft: LoanApplicationDraft, now: OffsetDateTime): Either<UpdateDraftErrors, LoanApplicationDraftUpdated> =
  if (draft.id != this.loanApplicationId) {
    UpdateDraftErrors.DraftNotFound.left()
  } else {
    when (this) {
      is UpdateDraftCommand.UpdateCustomerDataCommand -> updateCustomer(this, draft, now)
      is UpdateDraftCommand.UpdateLoanDataCommand -> updateLoan(this, draft, now)
      is UpdateDraftCommand.UpdatePropertyDataCommand -> updateProperty(this, draft, now)
    }
  }

private fun updateProperty(command: UpdateDraftCommand.UpdatePropertyDataCommand, draft: LoanApplicationDraft, now: OffsetDateTime): Either<UpdateDraftErrors, LoanApplicationDraftUpdated> =
  draft.copy(property = PropertyDraft(
    value = command.value,
    address = AddressDraft(
      street = command.propertyStreet,
      city = command.propertyCity,
      country = command.propertyCountry,
      zipCode = command.propertyZipCode
    )
  )).let {
    val errorMessage = it.validationErrors().fold(
      { LoanApplicationDraftUpdated.LoanApplicationDraftChange.DraftBecameValid },
      { LoanApplicationDraftUpdated.LoanApplicationDraftChange.DraftBecameInvalid(it) }
    )
    LoanApplicationDraftUpdated(
      aggregateId = draft.id,
      occurredAt = now,
      changes = Nel(errorMessage, listOf(LoanApplicationDraftUpdated.LoanApplicationDraftChange.PropertyDataChanged(it.property)))
    ).right()
  }

// pure domain logic without domain events
private fun updateLoan(command: UpdateDraftCommand.UpdateLoanDataCommand, draft: LoanApplicationDraft, now: OffsetDateTime): Either<UpdateDraftErrors, LoanApplicationDraftUpdated> =
  draft.copy(loan = LoanDraft(
    amount = command.amount,
    duration = command.duration,
    interestRate = command.interestRate)).let {
    val errorMessage = it.validationErrors().fold(
      { LoanApplicationDraftUpdated.LoanApplicationDraftChange.DraftBecameValid },
      { LoanApplicationDraftUpdated.LoanApplicationDraftChange.DraftBecameInvalid(it) }
    )
    LoanApplicationDraftUpdated(
      aggregateId = draft.id,
      occurredAt = now,
      changes = Nel(errorMessage, listOf(LoanApplicationDraftUpdated.LoanApplicationDraftChange.LoanDataChanged(it.loan)))
    ).right()
  }

// pure domain logic without domain events
private fun updateCustomer(command: UpdateDraftCommand.UpdateCustomerDataCommand, draft: LoanApplicationDraft, now: OffsetDateTime): Either<UpdateDraftErrors, LoanApplicationDraftUpdated> =
  draft.copy(customer = CustomerDraft(
    firstName = command.customerFirstName,
    lastName = command.customerLastName,
    birthDate = command.customerBirthDate,
    monthlyIncome = command.customerMonthlyIncome,
    address = AddressDraft(
      street = command.customerStreet,
      city = command.customerCity,
      country = command.customerCountry,
      zipCode = command.customerZipCode
    )
  )).let {
    val errorMessage = it.validationErrors().fold(
      { LoanApplicationDraftUpdated.LoanApplicationDraftChange.DraftBecameValid },
      { LoanApplicationDraftUpdated.LoanApplicationDraftChange.DraftBecameInvalid(it) }
    )
    LoanApplicationDraftUpdated(
      aggregateId = draft.id,
      occurredAt = now,
      changes = Nel(errorMessage, listOf(LoanApplicationDraftUpdated.LoanApplicationDraftChange.CustomerDataChanged(it.customer)))
    ).right()
  }
