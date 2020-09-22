package onionsoup.loanapplication

import arrow.core.Option
import arrow.core.getOrElse
import core.types.ValidationMessages
import io.javalin.http.Context
import onionsoup.loanapplication.LoanApplicationDraftController.Companion.BASE_PATH
import onionsoup.loanapplication.UpdateDraftCommand.UpdateCustomerDataCommand
import onionsoup.loanapplication.readmodel.CustomerData
import onionsoup.loanapplication.readmodel.CustomerDataDto
import onionsoup.loanapplication.readmodel.LoanApplicationDetails
import onionsoup.loanapplication.readmodel.LoanData
import onionsoup.loanapplication.readmodel.LoanDataDto
import onionsoup.loanapplication.readmodel.PropertyData
import onionsoup.loanapplication.readmodel.PropertyDataDto
import java.math.BigDecimal
import java.time.LocalDate

data class PropertyForm(
  val loanApplicationId: String,
  private val property: PropertyData
) : PropertyData by property {

  val action = "$BASE_PATH/$loanApplicationId/property"

  fun messagesFor(key: String): List<String> =
    Option.fromNullable(property._messages[key]).getOrElse { emptyList() }

  companion object {

    private fun Context.toPropertyData(): PropertyData =
      PropertyDataDto(
        value = this.formParam("value", BigDecimal::class.java).getOrNull(),
        propertyStreet = this.formParam("propertyStreet"),
        propertyZipCode = this.formParam("propertyZipCode"),
        propertyCity = this.formParam("propertyCity"),
        propertyCountry = this.formParam("propertyCountry"),
        _messages = ValidationMessages.empty()
      )

    fun PropertyData.intoPropertyForm(id: String): PropertyForm =
      PropertyForm(
        loanApplicationId = id,
        property = this
      )

    fun Context.intoPropertyForm(id: String): PropertyForm {
      return PropertyForm(
        loanApplicationId = id,
        property = this.toPropertyData()
      )
    }
  }
}

data class LoanForm(
  val loanApplicationId: String,
  private val loan: LoanData,
  val messages: Map<String, List<String>>
) : LoanData by loan {

  val action = "$BASE_PATH/$loanApplicationId/loan"

  companion object {

    fun LoanData.intoLoanForm(id: String): LoanForm =
      LoanForm(
        loanApplicationId = id,
        messages = emptyMap(),
        loan = this
      )

    fun Context.intoLoanForm(id: String): LoanForm {
      val messages = mutableMapOf<String, List<String>>()
      val amount = this.formParam("amount", BigDecimal::class.java)
      val duration = this.formParam("duration", Int::class.java)
      val interestRate = this.formParam("interestRate", BigDecimal::class.java)
      return LoanForm(
        loanApplicationId = id,
        messages = messages,
        loan = LoanDataDto(
          amount = amount.getOrNull(),
          duration = duration.getOrNull(),
          interestRate = interestRate.getOrNull(),
          _messages = ValidationMessages.empty()
        )
      )
    }
  }
}

data class CustomerForm(
  val loanApplicationId: String,
  val customer: CustomerData,
  val messages: Map<String, List<String>>
) : CustomerData by customer {

  val action = "$BASE_PATH/$loanApplicationId/customer"

  fun toCommand(id: String, requestId: String): UpdateCustomerDataCommand = UpdateCustomerDataCommand(
    loanApplicationId = id,
    customerFirstName = this.customer.customerFirstName,
    customerLastName = this.customer.customerLastName,
    customerBirthDate = this.customer.customerBirthDate,
    customerMonthlyIncome = this.customer.customerMonthlyIncome,
    customerStreet = this.customer.customerStreet,
    customerZipCode = this.customer.customerZipCode,
    customerCity = this.customer.customerCity,
    customerCountry = this.customer.customerCountry,
    requestId = requestId)

  companion object {
    fun LoanApplicationDetails.intoForm(): CustomerForm =
      CustomerForm(
        loanApplicationId = this.loanApplicationId,
        customer = this.customer,
        messages = emptyMap()
      )

    fun Context.intoCustomerForm(id: String): CustomerForm {
      val messages = mutableMapOf<String, List<String>>()
      val customerBirthDate = this.formParam("customerBirthDate", LocalDate::class.java)
      val customerMonthlyIncome = this.formParam("customerMonthlyIncome", BigDecimal::class.java)
      messages.putAll(customerBirthDate.errors())
      messages.putAll(customerMonthlyIncome.errors())
      return CustomerForm(
        loanApplicationId = id,
        messages = messages,
        customer = CustomerDataDto(
          _messages = ValidationMessages.empty(),
          customerFirstName = this.formParam("customerFirstName"),
          customerLastName = this.formParam("customerLastName"),
          customerBirthDate = customerBirthDate.getOrNull(),
          customerMonthlyIncome = customerMonthlyIncome.getOrNull(),
          customerStreet = this.formParam("customerStreet"),
          customerZipCode = this.formParam("customerZipCode"),
          customerCity = this.formParam("customerCity"),
          customerCountry = this.formParam("customerCountry")
        )
      )
    }
  }
}

data class LoanApplicationDetailsForm(
  val details: LoanApplicationDetails) {
  val submitAction = if (details.isValid) "$BASE_PATH/${details.loanApplicationId}/submit" else null
  val autofillAction = "$BASE_PATH/${details.loanApplicationId}/autofill"
  val editCustomer = if (details.state == "draft") "${BASE_PATH}/${details.loanApplicationId}/customer/edit" else null
  val editLoan = if (details.state == "draft") "${BASE_PATH}/${details.loanApplicationId}/loan/edit" else null
  val editProperty = if (details.state == "draft") "${BASE_PATH}/${details.loanApplicationId}/property/edit" else null
}

