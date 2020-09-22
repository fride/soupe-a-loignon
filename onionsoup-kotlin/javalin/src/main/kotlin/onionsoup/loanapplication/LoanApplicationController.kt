package onionsoup.loanapplication

import arrow.core.Nel
import arrow.fx.IO
import arrow.fx.extensions.fx
import core.extensions.getOrRaiseIoError
import io.javalin.http.Handler
import io.javalin.http.NotFoundResponse
import onionsoup.Action
import onionsoup.Action.Companion.requestId
import onionsoup.loanapplication.readmodel.DashboardQuery
import onionsoup.loanapplication.readmodel.State
import onionsoup.loanapplication.readmodel.StateFilter

class LoanApplicationController(private val module: LoanApplicationModule) {

  fun index(): Handler = Action.template { _ ->
    IO.fx {
      val dashboard = !module.loadDashboard(DashboardQuery(
        offset = 0,
        pageSize = 20,
        stateFilter = StateFilter.HavingState(Nel(State.SUBMITTED, listOf(State.ACCEPTED, State.REJECTED)))))
      "${TEMPLATE_BASE_PATH}/index.peb.html" to mapOf(Pair("dashboard", dashboard))
    }
  }

  fun view() = Action.template { ctx ->
    IO.fx {
      val loanApplicationId = ctx.pathParam("loan-application-id")
      val loanApplicationDetails = !module.findLoanApplicationDetails(loanApplicationId)
        .getOrRaiseIoError { NotFoundResponse() }
      "${TEMPLATE_BASE_PATH}/view.peb.html" to mapOf(
        "form" to LoanApplicationDetailsForm(loanApplicationDetails)
      )
    }
  }

  fun score() = Action.redirectTo { ctx ->
    IO.fx {
      val result = !module.scoreLoanApplicationUseCase(ScoreLoanApplicationCommand(
        applicationId = ctx.pathParam("loan-application-id"),
        requestId = ctx.requestId()
      ))
      "/loan_application"
    }
  }

  companion object {

    internal const val BASE_PATH = "/loan_application"
    private const val TEMPLATE_BASE_PATH = "templates/loan_application"

    internal fun showUri(id: String) = "${BASE_PATH}/${id}"
    internal fun persistUri(id: String, what: String) = "${BASE_PATH}/${id}/${what}"
  }
}
