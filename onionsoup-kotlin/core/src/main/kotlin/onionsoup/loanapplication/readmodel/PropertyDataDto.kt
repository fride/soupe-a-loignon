package onionsoup.loanapplication.readmodel

import core.types.ValidationMessages
import onionsoup.loanapplication.PropertyDraft
import java.math.BigDecimal

interface PropertyData {
  val value: BigDecimal?
  val propertyStreet: String?
  val propertyZipCode: String?
  val propertyCity: String?
  val propertyCountry: String?
  val _messages: ValidationMessages
}

data class PropertyDataDto(
  override val value: BigDecimal?,
  override val propertyStreet: String?,
  override val propertyZipCode: String?,
  override val propertyCity: String?,
  override val propertyCountry: String?,
  override val _messages: ValidationMessages
) : PropertyData {
  companion object {
    fun emptyProperty(): PropertyDataDto = PropertyDataDto(
      null,
      null,
      null,
      null,
      null,
      _messages = ValidationMessages.empty()
    )

    fun PropertyDraft.toPropertyData(): PropertyDataDto = PropertyDataDto(
      value = this.value,
      propertyCountry = this.address.country,
      propertyCity = this.address.city,
      propertyZipCode = this.address.zipCode,
      propertyStreet = this.address.street,
      _messages = ValidationMessages.empty()
    )
  }
}
