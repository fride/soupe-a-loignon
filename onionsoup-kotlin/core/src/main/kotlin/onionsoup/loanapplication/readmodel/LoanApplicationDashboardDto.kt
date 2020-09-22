package onionsoup.loanapplication.readmodel

import arrow.core.Nel
import java.time.OffsetDateTime

enum class State {
  DRAFT,
  SUBMITTED,
  ACCEPTED,
  REJECTED
}
sealed class StateFilter {
  object All : StateFilter()
  data class HavingState(val state: Nel<State>) : StateFilter()

}
data class DashboardQuery(val offset: Int,
                          val pageSize: Int,
                          val stateFilter: StateFilter = StateFilter.All)

data class LoanApplicationSummaryDto(
  val id: String,
  val name: String,
  val changedAt: OffsetDateTime,
  val state: String
)

data class LoanApplicationDashboardDto(
  val offset: Int,
  val pageSize: Int,
  val numberOfDrafts: Int,
  val loanApplications: List<LoanApplicationSummaryDto>
)
