package onionsoup.loanapplication.persistence

import arrow.core.Nel
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.spec.style.FunSpec
import onionsoup.loanapplication.AddressDraft
import onionsoup.loanapplication.CustomerDraft
import onionsoup.loanapplication.LoanApplicationDraftUpdated.LoanApplicationDraftChange
import onionsoup.loanapplication.LoanApplicationDraftUpdated
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_CUSTOMER
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_ID
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_LOAN
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_PROPERTY
import org.jooq.SQLDialect
import org.jooq.impl.DefaultConfiguration
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

fun datasource(): HikariDataSource? {
  val config = HikariConfig()
  config.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres")
  config.setUsername("postgres")
  config.setPassword("postgres")
  config.addDataSourceProperty("cachePrepStmts", "true")
  config.addDataSourceProperty("prepStmtCacheSize", "250")
  config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
  return HikariDataSource(config)
}
 class LoanApplicationDraftDataChangedHandlerTest : FunSpec({

  val config = DefaultConfiguration().let {
    it.setSQLDialect(SQLDialect.POSTGRES)
    it.setDataSource(datasource())
    it
  }

  val creationTime = OffsetDateTime.now().toLocalDateTime()

  beforeTest {
    config.dsl().transaction { ctx ->
      val dsl = ctx.dsl()
      dsl.delete(LOAN_APPLICATION_PROPERTY).where(LOAN_APPLICATION_PROPERTY.LOAN_APPLICATION_ID.eq("12")).execute()
      dsl.delete(LOAN_APPLICATION_LOAN).where(LOAN_APPLICATION_LOAN.LOAN_APPLICATION_ID.eq("12")).execute()
      dsl.delete(LOAN_APPLICATION_CUSTOMER).where(LOAN_APPLICATION_CUSTOMER.LOAN_APPLICATION_ID.eq("12")).execute()

      dsl.insertInto(LOAN_APPLICATION_ID,
        LOAN_APPLICATION_ID.ID,
        LOAN_APPLICATION_ID.CREATED_AT,
        LOAN_APPLICATION_ID.MODIFIED_AT)
        .values("12", creationTime, creationTime)
        .onConflict()
        .doUpdate()
        .set(LOAN_APPLICATION_ID.CREATED_AT, creationTime)
        .set(LOAN_APPLICATION_ID.MODIFIED_AT, creationTime)
        .execute()
    }
  }

  context("LoanApplicationDraftDataChangedHandler") {
    val handler = JooqPersistLoanApplicationDraftChanged(config)
    test("invoke should write to the database") {
      val action = handler(LoanApplicationDraftUpdated(
        aggregateId = "12",
        occurredAt = OffsetDateTime.now(),
        changes = Nel(LoanApplicationDraftChange.CustomerDataChanged(CustomerDraft(
          firstName = "firstName",
          lastName = "lastName",
          birthDate = LocalDate.now().minusYears(12),
          monthlyIncome = BigDecimal("144"),
          address = AddressDraft.emptyAddress
        )))))
      action.unsafeRunSync()
    }
  }
})
