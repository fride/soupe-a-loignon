package onionsoup

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.delete
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.apibuilder.ApiBuilder.put
import io.javalin.core.security.SecurityUtil.roles
import io.javalin.core.util.RouteOverviewPlugin
import io.javalin.core.validation.JavalinValidation
import io.javalin.http.Handler
import io.javalin.plugin.metrics.MicrometerPlugin
import io.javalin.plugin.rendering.JavalinRenderer
import io.javalin.plugin.rendering.template.JavalinPebble
import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import onionsoup.loanapplication.LoanApplicationController
import onionsoup.loanapplication.LoanApplicationDraftController
import onionsoup.loanapplication.LoanApplicationModule
import onionsoup.loanapplication.OperatorController
import onionsoup.loanapplication.persistence.JooqLoanApplicationModule
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

fun setupConverters() {
  JavalinValidation.register(
    LocalDate::class.java,
    converter = {
      if (it.isNullOrEmpty()) {
        null
      } else {
        LocalDate.parse(it)
      }
    }
  )
  JavalinValidation.register(
    BigDecimal::class.java,
    converter = {
      if (it.isNullOrEmpty()) {
        null
      } else {
        BigDecimal(it)
      }
    }
  )
}


fun createApp(): Javalin {
  JavalinRenderer.register(JavalinPebble, ".peb", ".html")
  setupConverters()
  val app = Javalin.create {
    it.addStaticFiles("/public")
    it.registerPlugin(MicrometerPlugin())
    // THIS IS NOT SECURE! :D
    it.accessManager(OnionAccessManger)
    it.registerPlugin(RouteOverviewPlugin("/routes", roles(OnionRole.Anyone)));
  }
  return app
}

fun createRoutes(app: Javalin, module: LoanApplicationModule) {
  val controller = LoanApplicationDraftController(module)
  val loanApplicationController = LoanApplicationController(module)
  val operatorController = OperatorController(
    module.createOperator,
    module.updateOperator,
    module.deleteOperator,
    module.getOperator,
    module.getAllOperators
  )

  app.routes {
    get("", Handler { it.redirect("/routes") })
    path("admin") {
      path("operator") {
        get(operatorController.getAll, roles(OnionRole.Admin))
        get("new", operatorController.new, roles(OnionRole.Admin))
        post(operatorController.create, roles(OnionRole.Admin))
        get("/:operator-id", operatorController.get, roles(OnionRole.Admin))
        put("/:operator-id", operatorController.update, roles(OnionRole.Admin))
        post("/:operator-id", operatorController.update, roles(OnionRole.Admin))
        delete("/:operator-id", operatorController.delete, roles(OnionRole.Admin))
        get("/:operator-id/edit", operatorController.edit, roles(OnionRole.Admin))
      }
    }
    path("login") {
      get(LoginController.get(), roles(OnionRole.Anyone))
      post(LoginController.post(), roles(OnionRole.Anyone))
    }

    path("loan_application") {

      path("draft") {
        get(controller.index(), roles(OnionRole.Operator))
        post(controller.create(), roles(OnionRole.Operator))

        path(":loan-application-id") {
          get(controller.view(), roles(OnionRole.Operator))
          post(controller.create(), roles(OnionRole.Operator))

          path("customer") {
            get("edit", controller.editCustomer(), roles(OnionRole.Operator))
            post(controller.persistCustomer(), roles(OnionRole.Operator))
          }
          path("loan") {
            get("edit", controller.editLoan(), roles(OnionRole.Operator))
            post(controller.persistLoan(), roles(OnionRole.Operator))
          }

          path("property") {
            get("edit", controller.editProperty(), roles(OnionRole.Operator))
            post(controller.persistProperty(), roles(OnionRole.Operator))
          }

          post("submit", controller.submitDraft(), roles(OnionRole.Operator))
          post("autofill", controller.createSillyDebugData(), roles(OnionRole.Operator))
        }
      }

      get(loanApplicationController.index(), roles(OnionRole.Operator))
      path(":loan-application-id") {
        get(loanApplicationController.view(), roles(OnionRole.Operator))
        post(loanApplicationController.score(), roles(OnionRole.Operator))
      }
    }
  }
}

fun main(args: Array<String>) {
  val config = ConfigFactory.load("application")
  val hikariConfig = config.extract<HikariDataSourceConfig>("data-source")
  val jooqConfig = hikariConfig.jooqConfiguration()
  val module = JooqLoanApplicationModule(clock = Clock.system(ZoneId.of("UTC")),
    configuration = jooqConfig,
    meterRegistry = LoggingMeterRegistry())
  val app = createApp()
  createRoutes(app, module)
  app.start(7000)
}
