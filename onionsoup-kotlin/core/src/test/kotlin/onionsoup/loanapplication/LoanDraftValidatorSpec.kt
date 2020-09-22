package onionsoup.loanapplication

import arrow.core.Valid
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.sequences.containAllInAnyOrder
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import onionsoup.loanapplication.LoanDraftValidator.validate
import onionsoup.loanapplication.LoanDraftValidator.validateAddress
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.test.assertEquals

class LoanDraftValidatorSpec : ExpectSpec({

  context("LoanApplicationDraft.validatedLoanApplication") {
    expect("returns Valid(LoanApplication) if a valid draft is passed into it") {
      val state = LoanApplicationState.Submitted(
        submissionDate = OffsetDateTime.now(),
        submittedBy = OperatorHandle("someone"))
      val draft = LoanApplicationDraft(
        id = "id",
        loan = LoanDraft(
          interestRate = BigDecimal("0.3"),
          duration = 23,
          amount = BigDecimal("10000")
        ),
        customer = CustomerDraft(
          address = AddressDraft(
            street = "street",
            city = "city",
            country = "country",
            zipCode = "0815"
          ),
          birthDate = LocalDate.now(),
          monthlyIncome = BigDecimal("3400"),
          firstName = "first name",
          lastName = "last name"
        ),
        property = PropertyDraft(
          address = AddressDraft(
            street = "street",
            city = "city",
            country = "country",
            zipCode = "0815"
          ),
          value = BigDecimal("23434345")
        ),
        operator = "smooth operator"
      )
      val loanApplication = draft.validate { id, customer, loan, property ->
        LoanApplication(id = id, customer = customer, property = property, loan = loan, state = state) }
      loanApplication shouldBe Valid(
        LoanApplication(
          id = "id",
          state = state,
          loan = Loan(
            interestRate = BigDecimal("0.3"),
            duration = 23,
            amount = BigDecimal("10000")
          ),
          customer = Customer(
            address = Address(
              street = Street("street"),
              city = City("city"),
              country = Country("country"),
              zipCode = ZipCode("0815")
            ),
            birthDate = LocalDate.now(),
            monthlyIncome = BigDecimal("3400"),
            name = Name("first name", "last name")
          ),
          property = Property(
            address = Address(
              street = Street("street"),
              city = City("city"),
              country = Country("country"),
              zipCode = ZipCode("0815")
            ),
            value = BigDecimal("23434345")
          )
        )
      )
    }
  }
  context("AddressDraft.validateAddress") {
    expect("accepts valid addresses") {
      val validatedAddress = AddressDraft(
        street = "street",
        zipCode = "zipCode",
        country = "country",
        city = "city"
      ).validateAddress()
      assertEquals(
        Valid(
          Address(
            street = Street("street"),
            zipCode = ZipCode("zipCode"),
            country = Country("country"),
            city = City("city")
          )
        ),
        validatedAddress
      )
    }
    expect("reject invalid addresses") {
      val validatedAddress = AddressDraft(
        street = "",
        zipCode = "zipCode",
        country = null,
        city = "city"
      ).validateAddress()
      validatedAddress.should { (containAllInAnyOrder(AddressValidationError.CountryMissing, AddressValidationError.StreetMissing)) }
    }
  }
})
