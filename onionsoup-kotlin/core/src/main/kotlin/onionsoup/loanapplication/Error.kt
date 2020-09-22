package onionsoup.loanapplication

import arrow.core.Nel
import arrow.core.Validated
import core.types.ValidationErrors
import java.math.BigDecimal

sealed class NewLoanApplicationError {
  object OperatorNotFound : NewLoanApplicationError()
}
sealed class UpdateDraftErrors {
  object DraftNotFound : UpdateDraftErrors()
}

sealed class SubmitLoanApplicationError {
  object DraftNotFound : SubmitLoanApplicationError()
  object DraftIsInvalid : SubmitLoanApplicationError()
}

sealed class AddressValidationError {
  object StreetMissing : AddressValidationError()
  object CountryMissing : AddressValidationError()
  object CityMissing : AddressValidationError()
  object ZipCodeMissing : AddressValidationError()
}

sealed class LoanValidationError {
  object AmountMissing : LoanValidationError()
  data class AmountToSmall(val minAmount: BigDecimal) : LoanValidationError()
  object DurationMissing : LoanValidationError()
  object InterestRateMissing : LoanValidationError()
  data class DurationToShort(val minDuration: Long) : LoanValidationError()
  data class DurationToLong(val maxDuration: Long) : LoanValidationError()
}

sealed class CustomerValidationError {
  data class AddressInvalid(val error: AddressValidationError) : CustomerValidationError()
  object FirstNameMissing : CustomerValidationError()
  object LastNameMissing : CustomerValidationError()
  object BirthDateMissing : CustomerValidationError()
  object MonthlyIncomeMissing : CustomerValidationError()
  object MonthlyIncomeToLow : CustomerValidationError()
}

sealed class PropertyValidationError {
  object PropertyValueMissing : PropertyValidationError()
  data class AddressInvalid(val error: AddressValidationError) : PropertyValidationError()
}

data class LoanApplicationValidationErrors(
  val customerValidationError: List<CustomerValidationError>,
  val loanValidationErrors: List<LoanValidationError>,
  val propertyValidationErrors: List<PropertyValidationError>
) : ValidationErrors

typealias ValidatedAddress = Validated<Nel<AddressValidationError>, Address>
typealias ValidatedLoan = Validated<Nel<LoanValidationError>, Loan>
typealias ValidatedCustomer = Validated<Nel<CustomerValidationError>, Customer>
typealias ValidatedProperty = Validated<Nel<PropertyValidationError>, Property>

sealed class ScoreLoanApplicationError {
  object LoanApplicationNotFound : ScoreLoanApplicationError()
  object LoanApplicationAlreadyScored : ScoreLoanApplicationError()
  object LoanApplicationAlreadyAccepted : ScoreLoanApplicationError()
  object LoanApplicationAlreadyRejected : ScoreLoanApplicationError()
}
