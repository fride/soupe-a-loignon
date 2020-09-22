package onionsoup

import core.extensions.utcNow
import core.types.ValidationMessages
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import onionsoup.DraftIndexPage.Companion.openIndexPage
import onionsoup.loanapplication.Name
import onionsoup.loanapplication.persistence.JooqLoanApplicationModule
import onionsoup.loanapplication.readmodel.CustomerDataDto
import onionsoup.loanapplication.readmodel.LoanApplicationDetails
import onionsoup.loanapplication.readmodel.LoanDataDto
import onionsoup.loanapplication.readmodel.PropertyDataDto
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * Example for a simple integration test that is actually faster then most mocked spring boot test.
 */
class CreateUpdateAndSubmitIntegrationTest : FunSpec({


  val config = Configuration().datasourceConfig.jooqConfiguration()
  val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
  val module = JooqLoanApplicationModule(clock = clock, configuration = config, meterRegistry = LoggingMeterRegistry())
  val app = createApp()
  val port = 7001
  val baseUri = "http://localhost:$port"

  createRoutes(app, module)

  beforeTest {
    app.start(port)
  }
  afterTest {
    app.stop()
  }

  test("create loan application draft, fill in nonsense and submit") {
    val driver: WebDriver = HtmlUnitDriver()
    driver.get(baseUri)
    driver.manage().addCookie(Cookie("onion-person", "test1", null, Date.from(LocalDate.now().plusDays(2).atStartOfDay(ZoneId.of("UTC")).toInstant())))
    val indexPage = driver.openIndexPage(baseUri)

    var viewPage = indexPage.createNewDraft()
    val id = viewPage.id
    viewPage
      .fillInRandomStuff()
      .navigateToViewPage(id)
      .submitLoanApplication()

    IndexPage.open(driver, baseUri)
      .navigateToViewPage(id)
      .customerContents shouldBe mapOf(
      "First Name" to "customerFirstName",
      "Last Name" to "customerLastName",
      "Birth Date" to "1985-08-22",
      "Monthly income" to "3333.00",
      "Street" to "customerStreet",
      "City" to "customerCity",
      "Zip Code" to "customerZipCode",
      "Country" to "customerCountry")

    val details = module.findLoanApplicationDetails(id).unsafeRunSync()

    details.orNull() shouldBe LoanApplicationDetails(
      loanApplicationId = id,
      customer = CustomerDataDto(
        customerFirstName = "customerFirstName",
        customerLastName = "customerLastName",
        customerMonthlyIncome = BigDecimal("3333").setScale(2),
        customerBirthDate = LocalDate.of(1985, 8, 22),
        customerCountry = "customerCountry",
        customerCity = "customerCity",
        customerZipCode = "customerZipCode",
        customerStreet = "customerStreet",
        _messages = ValidationMessages.empty()
      ),
      loan = LoanDataDto(
        amount = BigDecimal("2345456").setScale(2),
        duration = 98,
        interestRate = BigDecimal("5").setScale(2),
        _messages = ValidationMessages.empty()
      ),
      property = PropertyDataDto(
        propertyCountry = "propertyCountry",
        propertyZipCode = "propertyZipCode",
        propertyStreet = "propertyStreet",
        value = BigDecimal("998765").setScale(2),
        propertyCity = "propertyCity",
        _messages = ValidationMessages.empty()),
      state = "final",
      changedAt = clock.utcNow(),
      operatorName = Name("some", "test user"),
      operatorHandle = "test1"
    )
  }
})
