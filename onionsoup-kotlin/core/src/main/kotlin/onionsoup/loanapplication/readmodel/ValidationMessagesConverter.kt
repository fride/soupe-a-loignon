package onionsoup.loanapplication.readmodel

import arrow.core.extensions.list.foldable.foldLeft
import core.types.ValidationMessages
import onionsoup.loanapplication.AddressValidationError
import onionsoup.loanapplication.CustomerValidationError
import onionsoup.loanapplication.LoanApplicationValidationErrors
import onionsoup.loanapplication.LoanValidationError
import onionsoup.loanapplication.PropertyValidationError
import kotlin.reflect.KProperty1

// TODO this is not nice. javax.validation might be better suited ?
data class ValidationMessagesDto(
  val customer: Map<String, List<String>>,
  val loan: Map<String, List<String>>,
  val property: Map<String, List<String>>
)

fun LoanApplicationValidationErrors.toValidationMessagesDto() =
  ValidationMessagesDto(
    loan = ValidationMessagesConverter.loanValidationMessages(this).asMap(),
    property = ValidationMessagesConverter.propertyValidationMessages(this).asMap(),
    customer = ValidationMessagesConverter.customerValidationMessages(this).asMap())

/**
 * I'm not sure if I like this.
 */
object ValidationMessagesConverter {

  // TODO move into core
  fun <A, B> MutableMap<String, List<String>>.addMessage(prop: KProperty1<A, B>, message: () -> String): MutableMap<String, List<String>> {
    this.merge(prop.name, listOf(message())) { a, b -> a + b }
    return this
  }

  // TODO move into core
  fun <A, B> KProperty1<A, B>.addMessage(message: () -> String): Map<String, List<String>> =
    mutableMapOf(this.name to listOf(message()))

  // TODO move into core?
  private fun <A> toMessages(errors: List<A>, converter: (A) -> Map<String, List<String>>): Map<String, List<String>> =
    errors.foldLeft(mapOf()) { acc, error ->
      acc + converter(error)
    }

  fun loanValidationMessages(errors: LoanApplicationValidationErrors): ValidationMessages =
    ValidationMessages(
      toMessages(errors.loanValidationErrors) {
        when (it) {
          is LoanValidationError.AmountMissing -> LoanData::amount.addMessage { "{required}" }
          is LoanValidationError.AmountToSmall -> LoanData::amount.addMessage { "{loanAmount.toSmall}" }
          is LoanValidationError.DurationMissing -> LoanData::duration.addMessage { "{required}" }
          is LoanValidationError.InterestRateMissing -> LoanData::interestRate.addMessage { "{required}" }
          is LoanValidationError.DurationToShort -> LoanData::duration.addMessage { "{loanDuration.toShort}" }
          is LoanValidationError.DurationToLong -> LoanData::duration.addMessage { "{loanDuration.toLong}" }
        }
      }
    )

  fun customerValidationMessages(errors: LoanApplicationValidationErrors): ValidationMessages =
    ValidationMessages(
      toMessages(errors.customerValidationError) {
        when (it) {
          is CustomerValidationError.AddressInvalid -> when (it.error) {
            AddressValidationError.StreetMissing -> CustomerData::customerStreet.addMessage { "required" }
            AddressValidationError.CountryMissing -> CustomerData::customerCountry.addMessage { "required" }
            AddressValidationError.CityMissing -> CustomerData::customerCity.addMessage { "required" }
            AddressValidationError.ZipCodeMissing -> CustomerData::customerZipCode.addMessage { "required" }
          }
          is CustomerValidationError.FirstNameMissing -> CustomerData::customerFirstName.addMessage() { "{required}" }
          is CustomerValidationError.LastNameMissing -> CustomerData::customerLastName.addMessage { "{required}" }
          is CustomerValidationError.BirthDateMissing -> CustomerData::customerBirthDate.addMessage { "{required}" }
          is CustomerValidationError.MonthlyIncomeMissing -> CustomerData::customerMonthlyIncome.addMessage { "{required}" }
          is CustomerValidationError.MonthlyIncomeToLow -> CustomerDataDto::customerMonthlyIncome.addMessage { "{toPoor.Sorry}" }
        }
      }
    )

  fun propertyValidationMessages(errors: LoanApplicationValidationErrors): ValidationMessages =
    ValidationMessages(
      toMessages(errors.propertyValidationErrors) {
        when (it) {
          is PropertyValidationError.PropertyValueMissing -> PropertyData::value.addMessage { "{required}" }
          is PropertyValidationError.AddressInvalid ->
            when (it.error) {
              AddressValidationError.StreetMissing -> PropertyData::propertyStreet.addMessage { "required" }
              AddressValidationError.CountryMissing -> PropertyData::propertyCountry.addMessage { "required" }
              AddressValidationError.CityMissing -> PropertyData::propertyCity.addMessage { "required" }
              AddressValidationError.ZipCodeMissing -> PropertyData::propertyZipCode.addMessage { "required" }
            }
        }
      }
    )
}
