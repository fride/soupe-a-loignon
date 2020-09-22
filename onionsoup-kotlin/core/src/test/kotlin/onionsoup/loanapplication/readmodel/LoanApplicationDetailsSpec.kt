package onionsoup.loanapplication.readmodel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import onionsoup.loanapplication.Name
import java.time.OffsetDateTime

internal class LoanApplicationDetailsSpec : FunSpec({
  test("LoanApplicationDetails.isValid") {
    LoanApplicationDetails(
      loanApplicationId = "12",
      customer = CustomerDataDto.emptyCustomerData(),
      loan = LoanDataDto.emptyLoanData(),
      property = PropertyDataDto.emptyProperty(),
      state = "draft",
      changedAt = OffsetDateTime.now(),
      operatorHandle = "",
      operatorName = Name("","")
    ).isValid shouldBe true
  }
})
