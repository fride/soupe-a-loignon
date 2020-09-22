package onionsoup.loanapplication

import arrow.core.Nel
import arrow.fx.IO
import arrow.fx.extensions.fx
import core.extensions.getOrRaiseIoError
import io.javalin.http.BadRequestResponse
import io.javalin.http.Handler
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.NotFoundResponse
import onionsoup.Action
import onionsoup.Action.Companion.getOr404
import onionsoup.Action.Companion.requestId
import onionsoup.OnionUser
import onionsoup.OnionUser.Companion.onionUser
import onionsoup.loanapplication.CustomerForm.Companion.intoCustomerForm
import onionsoup.loanapplication.CustomerForm.Companion.intoForm
import onionsoup.loanapplication.LoanForm.Companion.intoLoanForm
import onionsoup.loanapplication.PropertyForm.Companion.intoPropertyForm
import onionsoup.loanapplication.readmodel.DashboardQuery
import onionsoup.loanapplication.readmodel.State
import onionsoup.loanapplication.readmodel.StateFilter
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID


class LoanApplicationDraftController(private val module: LoanApplicationModule) : LoanApplicationModule by module {

  fun index(): Handler = Action.template { _ ->
    IO.fx {
      val dashboard = !loadDashboard(DashboardQuery(
        offset = 0,
        pageSize = 20,
        stateFilter = StateFilter.HavingState(Nel(State.DRAFT))
      ))
      "${TEMPLATE_BASE_PATH}/index.peb.html" to mapOf(Pair("dashboard", dashboard))
    }
  }

  fun create() = Action.redirectTo { ctx ->
    val operator: OnionUser = ctx.onionUser()
    newLoanApplicationUseCase(NewLoanApplicationCommand(UUID.randomUUID().toString(),
      operatorHandle = operator.login,
      requestId = ctx.requestId()))
      .getOrRaiseIoError {
        when(it) {
          NewLoanApplicationError.OperatorNotFound -> BadRequestResponse("Operator does not exist")
        }
      }
      .map { showUri(it.aggregateId) }
  }

  fun view() = Action.template { ctx ->
    IO.fx {
      val loanApplicationId = ctx.pathParam("loan-application-id")
      val loanApplicationDetails = !findLoanApplicationDetails(loanApplicationId)
        .getOrRaiseIoError { NotFoundResponse() }
      "${TEMPLATE_BASE_PATH}/view.peb.html" to mapOf(
        "form" to LoanApplicationDetailsForm(loanApplicationDetails)
      )
    }
  }

  fun editCustomer() = Action.template { ctx ->
    module.findLoanApplicationDetails(ctx.pathParam("loan-application-id"))
      .getOrRaiseIoError { NotFoundResponse() }
      .map { details ->
        "${TEMPLATE_BASE_PATH}/edit_customer.peb.html" to mapOf(
          "draft" to details.intoForm(),
          "cancel" to "${BASE_PATH}/${details.loanApplicationId}"
        )
      }
  }

  fun persistCustomer() = Action.redirectTo { ctx ->
    IO.fx {
      val id = ctx.pathParam("loan-application-id")
      val form = ctx.intoCustomerForm(id) // ignore errors
      val command = form
        .toCommand(id, ctx.requestId())
      val result = !module.updateLoanApplicationUseCase(command)
        .getOrRaiseIoError { InternalServerErrorResponse() } // TODO we might send some nicer data here ;)
      showUri(result.aggregateId)
    }
  }

  fun editLoan() = Action.template { ctx ->
    IO.fx {
      val id = ctx.pathParam("loan-application-id")
      val details = !module.findLoanApplicationDetails(id).getOr404()
      "${TEMPLATE_BASE_PATH}/edit_loan.peb.html" to
        mapOf(
          "draft" to details.loan.intoLoanForm(id),
          "cancel" to showUri(details.loanApplicationId)
        )
    }
  }

  fun persistLoan() = Action.redirectTo { ctx ->
    IO.fx {
      val id = ctx.pathParam("loan-application-id")
      val form = ctx.intoLoanForm(id) // ignore errors
      val command = UpdateDraftCommand.UpdateLoanDataCommand(
        loanApplicationId = id,
        amount = form.amount,
        interestRate = form.interestRate,
        duration = form.duration,
        requestId = ctx.requestId()
      )
      val result = !updateLoanApplicationUseCase(command).getOrRaiseIoError { BadRequestResponse() }
      showUri(result.aggregateId)
    }
  }

  fun editProperty() = Action.template { ctx ->
    IO.fx {
      val id = ctx.pathParam("loan-application-id")
      val details = !findLoanApplicationDetails(id).getOr404()
      "${TEMPLATE_BASE_PATH}/edit_property.peb.html" to mapOf(
        "form" to details.property.intoPropertyForm(id),
        "cancel" to showUri(details.loanApplicationId)
      )
    }
  }

  fun persistProperty() = Action.redirectTo { ctx ->
    IO.fx {
      val id = ctx.pathParam("loan-application-id")
      val form = ctx.intoPropertyForm(id) // ignore errors
      val command = UpdateDraftCommand.UpdatePropertyDataCommand(
        loanApplicationId = id,
        value = form.value,
        propertyStreet = form.propertyStreet,
        propertyZipCode = form.propertyZipCode,
        propertyCity = form.propertyCity,
        propertyCountry = form.propertyCountry,
        requestId = ctx.requestId()
      )
      val response = !updateLoanApplicationUseCase(command).getOrRaiseIoError { BadRequestResponse() }
      showUri(response.aggregateId)
    }
  }

  fun submitDraft() = Action.redirectTo { ctx ->
    IO.fx {
      val id = ctx.pathParam("loan-application-id")
      val operator = !module.getOperator(ctx.onionUser().login).getOrRaiseIoError { BadRequestResponse("Operator does not exist") }
      !submitLoanApplicationDraftUseCase(SubmitLoanApplicationCommand(id, operatorHandle = OperatorHandle(operator.operatorHandle), requestId = ctx.requestId()))
        .getOrRaiseIoError {
          when (it) {
            SubmitLoanApplicationError.DraftIsInvalid -> BadRequestResponse()
            SubmitLoanApplicationError.DraftNotFound -> NotFoundResponse()
          }
        }.map { BASE_PATH }
    }
  }

  // just a debug function to fill in some test data. Ignore it please ;)
  fun createSillyDebugData() = Action.redirectTo { ctx ->
    IO.fx {
      val id = ctx.pathParam("loan-application-id")
      !updateLoanApplicationUseCase(UpdateDraftCommand.UpdateLoanDataCommand(
        loanApplicationId = id,
        amount = BigDecimal("2345456"),
        duration = 98,
        requestId = ctx.requestId(),
        interestRate = BigDecimal("5"))).getOrRaiseIoError { InternalServerErrorResponse() }

      !updateLoanApplicationUseCase(UpdateDraftCommand.UpdatePropertyDataCommand(
        loanApplicationId = id,
        propertyCountry = "propertyCountry",
        propertyZipCode = "propertyZipCode",
        propertyStreet = "propertyStreet",
        value = BigDecimal("998765"),
        requestId = ctx.requestId(),
        propertyCity = "propertyCity")).getOrRaiseIoError { InternalServerErrorResponse() }

      !updateLoanApplicationUseCase(UpdateDraftCommand.UpdateCustomerDataCommand(
        loanApplicationId = id,
        customerFirstName = "customerFirstName",
        customerLastName = "customerLastName",
        customerMonthlyIncome = BigDecimal("3333"),
        customerBirthDate = LocalDate.of(1985, 8,22),
        customerCountry = "customerCountry",
        customerCity = "customerCity",
        customerZipCode = "customerZipCode",
        customerStreet = "customerStreet",
        requestId = ctx.requestId()
      ))
      BASE_PATH
    }
  }
  companion object {

    internal const val BASE_PATH="/loan_application/draft"
    private const val TEMPLATE_BASE_PATH="templates/loan_application/draft"

    internal fun showUri(id: String) = "${BASE_PATH}/${id}"
    internal fun persistUri(id: String, what: String) = "${BASE_PATH}/${id}/${what}"
  }
}

