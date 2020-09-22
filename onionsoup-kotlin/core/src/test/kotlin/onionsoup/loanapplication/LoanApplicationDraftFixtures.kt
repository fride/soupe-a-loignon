package onionsoup.loanapplication

import arrow.fx.IO
import core.types.ModelUpdater
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

object LoanApplicationDraftFixtures {
  val completeLoanApplicationDraft = LoanApplicationDraft(
    id = "12",
    customer = CustomerDraft(
      firstName = "first name",
      lastName = "last name",
      birthDate = LocalDate.now().minusYears(30),
      monthlyIncome = BigDecimal("6666"),
      address = AddressDraft(
        street = "street",
        zipCode = "zipcode",
        country = "country",
        city = "city"
      )
    ),
    loan = LoanDraft(
      interestRate = BigDecimal("0.3"),
      duration = 98,
      amount = BigDecimal("90000")
    ),
    property = PropertyDraft(
      value = BigDecimal("80000"),
      address = AddressDraft(
        street = "street",
        zipCode = "zipcode",
        country = "country",
        city = "city"
      )
    ),
    operator = "smooth operator"
  )

  val loanApplication = LoanApplication(
    id = "12",
    customer = Customer(
      name = Name("first name", "last name"),
      birthDate = LocalDate.now().minusYears(30),
      monthlyIncome = BigDecimal("6666"),
      address = Address(
        street = Street("street"),
        zipCode = ZipCode("zipcode"),
        country = Country("country"),
        city = City("city")
      )
    ),
    loan = Loan(
      interestRate = BigDecimal("0.3"),
      duration = 98,
      amount = BigDecimal("90000")
    ),
    property = Property(
      value = BigDecimal("80000"),
      address = Address(
        street = Street("street"),
        zipCode = ZipCode("zipcode"),
        country = Country("country"),
        city = City("city")
      )
    ),
    state = LoanApplicationState.Submitted(
      submittedBy = OperatorHandle("12"),
      submissionDate = OffsetDateTime.now()
    )
  )

  fun <A> noOpModelUpdater(): ModelUpdater<A> = { IO.unit }
}
