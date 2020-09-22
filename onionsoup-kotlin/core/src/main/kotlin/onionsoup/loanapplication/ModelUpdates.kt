package onionsoup.loanapplication

import arrow.core.Nel
import arrow.core.Option
import core.types.ModelUpdate
import java.time.OffsetDateTime

sealed class LoanApplicationModelUpdate : ModelUpdate

data class LoanApplicationCreated(
  override val aggregateId: String,
  override val occurredAt: OffsetDateTime,
  val operatorHandle: String,
  val validationErrors: Option<LoanApplicationValidationErrors>
) : LoanApplicationModelUpdate()


data class LoanApplicationDraftUpdated(
  override val aggregateId: String,
  override val occurredAt: OffsetDateTime,
  val changes: Nel<LoanApplicationDraftChange>
) : LoanApplicationModelUpdate() {

  sealed class LoanApplicationDraftChange {
    data class CustomerDataChanged(val customerData: CustomerDraft) : LoanApplicationDraftChange()
    data class LoanDataChanged(val loanData: LoanDraft) : LoanApplicationDraftChange()
    data class PropertyDataChanged(val propertyData: PropertyDraft) : LoanApplicationDraftChange()
    data class DraftBecameInvalid(val errors: LoanApplicationValidationErrors) : LoanApplicationDraftChange()
    object DraftBecameValid : LoanApplicationDraftChange()
  }
}

data class LoanApplicationSubmitted(
  override val aggregateId: String,
  override val occurredAt: OffsetDateTime,
  val operator: OperatorHandle,
  val loan: Loan,
  val customer: Customer,
  val property: Property
) : LoanApplicationModelUpdate()

data class LoanApplicationScored(
  override val aggregateId: String,
  override val occurredAt: OffsetDateTime,
  val score: LoanApplicationScore
) : LoanApplicationModelUpdate()
