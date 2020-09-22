package onionsoup.loanapplication

import arrow.core.Invalid
import arrow.core.Left
import arrow.core.Nel
import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Right
import arrow.core.Some
import arrow.core.Valid
import arrow.core.Validated
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicative.applicative
import arrow.core.fix
import arrow.core.nel
import core.types.check
import core.types.errors
import core.types.max
import core.types.min
import core.types.notEmpty
import core.types.required
import java.math.BigDecimal

internal object LoanDraftValidator {

  fun AddressDraft.validateAddress(): ValidatedAddress {
    return Validated.applicative<Nel<AddressValidationError>>(NonEmptyList.semigroup()).mapN(
      this.street.notEmpty { AddressValidationError.StreetMissing }.map(::Street),
      this.zipCode.notEmpty { AddressValidationError.ZipCodeMissing }.map(::ZipCode),
      this.city.notEmpty { AddressValidationError.CityMissing }.map(::City),
      this.country.notEmpty { AddressValidationError.CountryMissing }.map(::Country)
    ) { (street, zipCode, city, country) ->
      Address(
        street = street,
        city = city,
        country = country,
        zipCode = zipCode
      )
    }.fix()
  }

  fun LoanDraft.validatedLoan(): ValidatedLoan {
    val vAmount = this.amount.required { LoanValidationError.AmountMissing }
      .min(BigDecimal.ZERO) { LoanValidationError.AmountToSmall(BigDecimal.ZERO) }
    val vDuration = this.duration.required { LoanValidationError.DurationMissing }
      .min(12) { LoanValidationError.DurationToShort(12) }
      .max(120) { LoanValidationError.DurationToLong(120) }
    val vInterestRate = this.interestRate.required { LoanValidationError.InterestRateMissing }
    return Validated.applicative<Nel<LoanValidationError>>(NonEmptyList.semigroup()).mapN(
      vAmount, vDuration, vInterestRate
    ) {
      Loan(
        amount = it.a,
        duration = it.b,
        interestRate = it.c
      )
    }.fix()
  }

  fun PropertyDraft.validatedProperty(): ValidatedProperty {
    val vAddress = this.address.validateAddress().leftMap { it.map(PropertyValidationError::AddressInvalid) }
    val vValue = this.value.required { PropertyValidationError.PropertyValueMissing }
    return Validated.applicative<Nel<PropertyValidationError>>(NonEmptyList.semigroup()).mapN(
      vAddress, vValue
    ) {
      Property(
        address = it.a,
        value = it.b
      )
    }.fix()
  }

  fun CustomerDraft.validateCustomer(): ValidatedCustomer {
    val vAddress = this.address.validateAddress()
      .leftMap { it.map(CustomerValidationError::AddressInvalid) }
    val vBirthDate = this.birthDate.required { CustomerValidationError.BirthDateMissing }
    val vFirstName = this.firstName.notEmpty { CustomerValidationError.FirstNameMissing }
    val vLastName = this.lastName.notEmpty { CustomerValidationError.LastNameMissing }
    val vMonthlyIncome = this.monthlyIncome.required { CustomerValidationError.MonthlyIncomeMissing }
      .check { if (it < BigDecimal(1000)) Left(CustomerValidationError.MonthlyIncomeToLow.nel()) else Right(it) }

    return Validated.applicative<Nel<CustomerValidationError>>(NonEmptyList.semigroup()).mapN(
      vAddress, vFirstName, vLastName, vMonthlyIncome, vBirthDate
    ) {
      Customer(
        address = it.a,
        name = Name(it.b, it.c),
        monthlyIncome = it.d,
        birthDate = it.e
      )
    }.fix()
  }

  fun <T> LoanApplicationDraft.validate(h: (String, Customer, Loan, Property) -> T): Validated<LoanApplicationValidationErrors, T> {
    val vCustomer = this.customer.validateCustomer()
    val vLoan = this.loan.validatedLoan()
    val vProperty = this.property.validatedProperty()

    return when {
      vCustomer is Valid && vLoan is Valid && vProperty is Valid -> Valid(h(id, vCustomer.a, vLoan.a, vProperty.a))
      else -> Invalid(
        LoanApplicationValidationErrors(
          customerValidationError = vCustomer.errors(),
          loanValidationErrors = vLoan.errors(),
          propertyValidationErrors = vProperty.errors()
        )
      )
    }
  }

  fun LoanApplicationDraft.validationErrors(): Option<LoanApplicationValidationErrors> =
    this.validate { _, _, _, _ -> Unit }.fold(
      { Some(it) },
      { None }
    )
}
