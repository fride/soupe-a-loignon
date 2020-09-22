package onionsoup.loanapplication.readmodel

import onionsoup.loanapplication.Name
import java.time.OffsetDateTime

data class LoanApplicationDetails(
  val loanApplicationId: String,
  val customer: CustomerDataDto,
  val loan: LoanDataDto,
  val property: PropertyDataDto,
  val state: String,
  val changedAt: OffsetDateTime,
  val operatorHandle: String,
  val operatorName: Name
) {
  val isValid: Boolean = customer._messages.isEmpty && loan._messages.isEmpty && property._messages.isEmpty
}
