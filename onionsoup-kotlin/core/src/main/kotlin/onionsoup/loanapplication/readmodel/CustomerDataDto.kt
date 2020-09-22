package onionsoup.loanapplication.readmodel

import core.types.ValidationMessages
import onionsoup.loanapplication.CustomerDraft
import java.math.BigDecimal
import java.time.LocalDate

interface CustomerData {
  val customerFirstName: String?
  val customerLastName: String?
  val customerBirthDate: LocalDate?
  val customerMonthlyIncome: BigDecimal?
  val customerStreet: String?
  val customerZipCode: String?
  val customerCity: String?
  val customerCountry: String?
  val _messages: ValidationMessages
}

data class CustomerDataDto(
  override val customerFirstName: String?,
  override val customerLastName: String?,
  override val customerBirthDate: LocalDate?,
  override val customerMonthlyIncome: BigDecimal?,
  override val customerStreet: String?,
  override val customerZipCode: String?,
  override val customerCity: String?,
  override val customerCountry: String?,
  override val _messages: ValidationMessages
) : CustomerData {
  companion object {
    fun emptyCustomerData(): CustomerDataDto = CustomerDataDto(
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      ValidationMessages.empty()
    )

    fun CustomerDraft.toCustomerData(): CustomerDataDto = CustomerDataDto(
      customerFirstName = this.firstName,
      customerLastName = this.lastName,
      customerBirthDate = this.birthDate,
      customerMonthlyIncome = this.monthlyIncome,
      customerStreet = this.address.street,
      customerZipCode = this.address.zipCode,
      customerCity = this.address.city,
      customerCountry = this.address.country,
      _messages = ValidationMessages.empty()
    )
  }
}
