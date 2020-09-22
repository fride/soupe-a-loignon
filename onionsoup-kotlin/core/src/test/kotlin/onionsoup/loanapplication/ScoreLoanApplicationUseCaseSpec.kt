package onionsoup.loanapplication

import arrow.core.Nel
import arrow.core.orNull
import arrow.core.some
import arrow.fx.IO
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

internal class ScoreLoanApplicationUseCaseSpec : FunSpec({
  val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))

  val loanApplication = LoanApplication(
    id = "12",
    customer = Customer(
      name = Name("first name", "last name"),
      birthDate = LocalDate.now().minusYears(56),
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
      duration = 12 * 10,
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

  test("score a submitted load application red and mark errors") {
    val useCase = ScoreLoanApplicationUseCase(
      findLoanApplication = { IO.just(loanApplication.some()) },
      clock = clock,
      getDebtorState = {
        IO.just(DebtorState.RegisteredDebtor)
      },
      persistLoanApplicationScored = { IO.unit }
    )
    val result = useCase(ScoreLoanApplicationCommand(
      applicationId = "12",
      requestId = "1"
    )).unsafeRunSync().orNull()!!
    result.score shouldBe LoanApplicationScore.Red(Nel(
      "Property value is lower than loan amount.",
      listOf(
        "Customer age at last installment date is above 65.",
        "Customer is registered in debtor registry")))
  }
})
