package onionsoup.loanapplication.persistence

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.list.foldable.foldLeft
import arrow.core.getOrElse
import arrow.core.orElse
import arrow.fx.IO
import arrow.fx.extensions.fx
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_DETAILS
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_ID
import onionsoup.loanapplication.readmodel.DashboardQuery
import onionsoup.loanapplication.readmodel.LoadDashboard
import onionsoup.loanapplication.readmodel.LoanApplicationDashboardDto
import onionsoup.loanapplication.readmodel.LoanApplicationSummaryDto
import onionsoup.loanapplication.readmodel.State
import onionsoup.loanapplication.readmodel.StateFilter
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.time.ZoneOffset

private fun name(first: Option<String>, last: Option<String>): String =
  when {
    first is Some && last is Some -> "${last.t},${first.t}"
    first is None && last is Some -> "Mx. ${last.t}"
    first is Some && last is None -> "${first.t}"
    else -> "unknown person"
  }

internal fun loadDashboard(dsl: DSLContext, query: DashboardQuery): IO<LoanApplicationDashboardDto> =
  IO.fx {
    val count = dsl.selectCount()
      .from(LOAN_APPLICATION_DETAILS)
      .execute()

    val select = dsl
      .select(
        LOAN_APPLICATION_DETAILS.ID,
        LOAN_APPLICATION_DETAILS.MODIFIED_AT,
        LOAN_APPLICATION_DETAILS.CUSTOMER_FIRST_NAME,
        LOAN_APPLICATION_DETAILS.CUSTOMER_LAST_NAME,
        LOAN_APPLICATION_DETAILS.SUBMITTED_AT,
        LOAN_APPLICATION_DETAILS.CHECKED_AT,
        LOAN_APPLICATION_DETAILS.REJECTED_AT,
        LOAN_APPLICATION_DETAILS.ACCEPTED_AT)
      .from(LOAN_APPLICATION_DETAILS)

    val condition = when (val filter = query.stateFilter) {
      is StateFilter.All -> DSL.condition(true)
      is StateFilter.HavingState -> {
        filter.state.all.foldLeft(DSL.condition(false)){condition, state ->
          when (state) {
            State.DRAFT -> condition.or(LOAN_APPLICATION_DETAILS.SUBMITTED_AT.isNull)
            State.SUBMITTED -> condition.or(LOAN_APPLICATION_DETAILS.SUBMITTED_AT.isNotNull)
            State.ACCEPTED -> condition.or(LOAN_APPLICATION_DETAILS.ACCEPTED_AT.isNotNull)
            State.REJECTED -> condition.or(LOAN_APPLICATION_DETAILS.REJECTED_AT.isNotNull)
          }
        }
      }
    }
    val summaries =
      select
      .where(condition)
      .orderBy(LOAN_APPLICATION_DETAILS.MODIFIED_AT.desc())
      .fetch()
      .map {
        LoanApplicationSummaryDto(
          id = it[LOAN_APPLICATION_ID.ID],
          changedAt = it[LOAN_APPLICATION_ID.MODIFIED_AT].atOffset(ZoneOffset.UTC),
          state =
          when {
              it[LOAN_APPLICATION_DETAILS.ACCEPTED_AT] != null -> {
                "👍"
              }
              it[LOAN_APPLICATION_DETAILS.REJECTED_AT] != null -> {
                "👎"
              }
              it[LOAN_APPLICATION_DETAILS.CHECKED_AT] != null -> {
                "🆗"
              }
              it[LOAN_APPLICATION_DETAILS.SUBMITTED_AT] != null -> {
                "⏱"
              }
              else -> {"🚧"}
          },
          name = name(
            Option.fromNullable(it[LOAN_APPLICATION_DETAILS.CUSTOMER_FIRST_NAME]),
            Option.fromNullable(it[LOAN_APPLICATION_DETAILS.CUSTOMER_LAST_NAME]))
        )
      }
    LoanApplicationDashboardDto(
      numberOfDrafts = count,
      loanApplications = summaries,
      pageSize = query.pageSize,
      offset = query.offset
    )
  }

fun jooqLoadDashboard(configuration: Configuration): LoadDashboard =
  { query -> configuration.readOnlyTransaction { dsl -> loadDashboard(dsl, query) } }
