package onionsoup.loanapplication

import io.kotest.core.spec.style.BehaviorSpec

class CreateAndUpdateLoanApplicationDraftSpec : BehaviorSpec({
//  val clock: Clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
//  val drafts: MutableMap<String, LoanApplicationDraft> = mutableMapOf()
//  val loanApplicationsDetails: MutableMap<String, LoanApplicationDetails> = mutableMapOf()
//
//
//  val updateDraftWorkflowB = updateLoanApplication(
//    findLoanDraft = persistence::findLoanApplication,
//    persistLoanApplicationDraftState = persistence.loanApplicationDraftDataChangedHandler,
//    clock = clock)
//
//  given("an existing draft with id 12") {
//    newLoanApplicationWorkflow(NewLoanApplicationCommand(loanApplicationId = "12", requestId = "foo")).unsafeRunSync() should beRight()
//    `when`("a customer is added to the draft") {
//      val res =
//        updateDraftWorkflowB(UpdateDraftCommand.UpdateCustomerDataCommand(
//          customerFirstName = "Mister",
//          customerLastName = "T.",
//          loanApplicationId = "12",
//          customerStreet = "Hollywood Boulevard 101",
//          customerZipCode = "00001",
//          customerMonthlyIncome = BigDecimal("8000"),
//          customerCountry = "USA",
//          customerCity = "Los Angeles",
//          customerBirthDate = clock.today().minusYears(25),
//          requestId = "foo"
//        )
//        ).unsafeRunSync()
//
//      res should beRight()
//    }
//    `when`("loan information is added") {
//      val res =
//        updateDraftWorkflowB(UpdateDraftCommand.UpdateLoanDataCommand(
//          loanApplicationId = "12",
//          interestRate = BigDecimal("0.3"),
//          amount = BigDecimal("666000"),
//          duration = 48,
//          requestId = "foo"
//        )
//        ).unsafeRunSync()
//
//      res should beRight()
//    }
//    `when`("property information is added") {
//      val res =
//        updateDraftWorkflowB(UpdateDraftCommand.UpdatePropertyDataCommand(
//          loanApplicationId = "12",
//          value = BigDecimal.ZERO, // yes, we accept nonsense data too ;)
//          propertyZipCode = "00000",
//          propertyStreet = "Hollywood Boulevard 102",
//          propertyCountry = "USA",
//          propertyCity = "Los Angeles",
//          requestId = "foo"
//        )
//        ).unsafeRunSync()
//      res should beRight()
//    }
//    then("the mus be a complete loan application draft") {
//      val details = loanApplicationsDetails["12"]!!
//      details.isValid shouldBe true
//      details shouldBe
//        LoanApplicationDetails(
//          loanApplicationId = "12",
//          changedAt = clock.utcNow(),
//          state = "draft",
//          property = PropertyDataDto(
//            value = BigDecimal.ZERO, // yes, we accept nonsense data too ;)
//            propertyZipCode = "00000",
//            propertyStreet = "Hollywood Boulevard 102",
//            propertyCountry = "USA",
//            propertyCity = "Los Angeles",
//            _messages = ValidationMessages.empty()
//          ),
//          customer = CustomerDataDto(
//            customerFirstName = "Mister",
//            customerLastName = "T.",
//            customerStreet = "Hollywood Boulevard 101",
//            customerZipCode = "00001",
//            customerMonthlyIncome = BigDecimal("8000"),
//            customerCountry = "USA",
//            customerCity = "Los Angeles",
//            customerBirthDate = clock.today().minusYears(25),
//            _messages = ValidationMessages.empty()
//          ),
//          loan = LoanDataDto(
//            interestRate = BigDecimal("0.3"),
//            amount = BigDecimal("666000"),
//            duration = 48,
//            _messages = ValidationMessages.empty()
//          )
//        )
//    }
//  }
})
