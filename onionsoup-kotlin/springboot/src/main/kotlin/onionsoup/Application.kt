package onionsoup

import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import onionsoup.loanapplication.LoanApplicationModule
import onionsoup.loanapplication.persistence.JooqLoanApplicationModule
import org.jooq.Configuration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Clock
import java.time.ZoneId

@SpringBootApplication
open class Application : WebMvcConfigurer {

  @Bean
  open fun clock(): Clock = Clock.system(ZoneId.of("UTC"))

  @Bean
  open fun loanApplicationModule(configuration: Configuration, clock: Clock): LoanApplicationModule {
    return JooqLoanApplicationModule(configuration, clock, LoggingMeterRegistry())
  }
}

fun main(args: Array<String>) {
  SpringApplication.run(Application::class.java, *args)
}
