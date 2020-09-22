package onionsoup.loanapplication
// contains only the data, aggregates, entities (none yet) and value objects

import arrow.core.Nel
import java.lang.IllegalStateException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Year
import java.time.temporal.ChronoUnit
import javax.xml.datatype.DatatypeConstants.YEARS

// drafts
data class AddressDraft(
  val street: String?,
  val zipCode: String?,
  val city: String?,
  val country: String?
) {
  companion object {
    val emptyAddress = AddressDraft(null, null, null, null)
  }
}

data class CustomerDraft(
  val firstName: String?,
  val lastName: String?,
  val birthDate: LocalDate?,
  val monthlyIncome: BigDecimal?,
  val address: AddressDraft
) {
  companion object {
    val emptyCustomer = CustomerDraft(null, null, null, null, AddressDraft.emptyAddress)
  }
}

data class LoanDraft(
  val amount: BigDecimal?,
  val duration: Int?,
  val interestRate: BigDecimal?
) {
  companion object {
    val emptyLoan = LoanDraft(null, null, null)
  }
}

data class PropertyDraft(
  val value: BigDecimal?,
  val address: AddressDraft
) {
  companion object {
    val emptyProperty = PropertyDraft(null, AddressDraft.emptyAddress)
  }
}

data class LoanApplicationDraft(
  val id: String,
  val operator: String,
  val customer: CustomerDraft,
  val loan: LoanDraft,
  val property: PropertyDraft
) {
  companion object {
    fun newDraft(id: String, operator: String) = LoanApplicationDraft(
      id = id,
      operator = operator,
      customer = CustomerDraft.emptyCustomer,
      loan = LoanDraft.emptyLoan,
      property = PropertyDraft.emptyProperty
    )
  }
}

// final applications

inline class Street(val value: String)

inline class ZipCode(val value: String)

inline class City(val value: String)

inline class Country(val value: String)

data class Address(
  val street: Street,
  val zipCode: ZipCode,
  val city: City,
  val country: Country
)

data class Name(val firstName: String, val secondName: String)

data class Customer(
  val name: Name,
  val birthDate: LocalDate,
  val monthlyIncome: BigDecimal,
  val address: Address) {
  fun ageAt(day: LocalDate) : Long =
    ChronoUnit.YEARS.between(birthDate, day)

}

data class Loan(
  val amount: BigDecimal,
  val duration: Int,
  val interestRate: BigDecimal) {
  fun lastInstallmentDate(startAt: LocalDate) : LocalDate =
    startAt.plusMonths(duration.toLong())
}

data class Property(
  val value: BigDecimal,
  val address: Address
)

sealed class LoanApplicationState {
  abstract val submittedBy: OperatorHandle
  abstract val submissionDate: OffsetDateTime

  data class Submitted(
    override val submittedBy: OperatorHandle,
    override val submissionDate: OffsetDateTime) : LoanApplicationState()

  data class Scored(
    override val submittedBy: OperatorHandle,
    override val submissionDate: OffsetDateTime,
    val scoringDate: OffsetDateTime,
    val score: LoanApplicationScore) : LoanApplicationState()

  data class Accepted(
    override val submittedBy: OperatorHandle,
    override val submissionDate: OffsetDateTime) : LoanApplicationState()

  data class Rejected(
    override val submittedBy: OperatorHandle,
    override val submissionDate: OffsetDateTime) : LoanApplicationState()
}

data class LoanApplication(
  val id: String,
  val customer: Customer,
  val loan: Loan,
  val property: Property,
  val state: LoanApplicationState)


inline class OperatorHandle(val handle: String)

data class Operator(
  val operatorHandle: String,
  val name: Name,
  val competenceLevel: BigDecimal
)

sealed class LoanApplicationScore {
  data class Red(val reasons: Nel<String>) : LoanApplicationScore()
  object Green : LoanApplicationScore()

  operator fun plus(other: LoanApplicationScore) : LoanApplicationScore=
    when {
      this is Red   && other is Red -> Red(this.reasons + other.reasons)
      this is Green && other is Red -> other
      this is Red   && other is Green -> this
      this is Green && other is Green -> this
      else -> throw IllegalStateException("Not possible")
    }
}

sealed class DebtorState{
  object RegisteredDebtor : DebtorState()
  object NotADebtor : DebtorState()
}
