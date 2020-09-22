package onionsoup.loanapplication

import onionsoup.loanapplication.readmodel.DashboardQuery
import onionsoup.loanapplication.readmodel.LoanApplicationDashboardDto
import onionsoup.loanapplication.readmodel.LoanApplicationDetails
import onionsoup.readTransaction
import org.springframework.http.ResponseEntity
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class LoanApplicationDraftController(
  private val module: LoanApplicationModule,
  private val tx: PlatformTransactionManager
) {

  @GetMapping("/api/loan_applications")
  fun index(): ResponseEntity<LoanApplicationDashboardDto> {
    module.loadDashboard(DashboardQuery(0, 1000))
      .readTransaction(tx)

    val res = module.loadDashboard(DashboardQuery(0, 1000))
      .readTransaction(tx)
    return ResponseEntity.ok(res)
  }

  @GetMapping("/api/loan_applications/{id}")
  fun details(@PathVariable id: String): ResponseEntity<LoanApplicationDetails> =
    module.findLoanApplicationDetails(id).unsafeRunSync().fold(
      { ResponseEntity.notFound().build() },
      { details -> ResponseEntity.ok(details) }
    )
}
