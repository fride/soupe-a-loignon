package onionsoup.loanapplication

import arrow.core.Nel
import arrow.core.extensions.list.foldable.foldLeft
import java.time.LocalDate

typealias ScoringRule = (LoanApplication) -> LoanApplicationScore

object ScoringRules {

  val loanAmountMustBeLowerThanPropertyValue: ScoringRule = { loanApplication ->
    if (loanApplication.loan.amount > loanApplication.property.value) {
      LoanApplicationScore.Red(Nel("Property value is lower than loan amount."))
    } else {
      LoanApplicationScore.Green
    }
  }

  fun customerAgeAtTheDateOfLastInstallmentMustBeBelow65(today: LocalDate): ScoringRule = { loanApplication ->
    val lastInstallmentDate = loanApplication.loan.lastInstallmentDate(today)
    if (loanApplication.customer.ageAt(lastInstallmentDate) < 65) {
      LoanApplicationScore.Green
    } else {
      LoanApplicationScore.Red(Nel("Customer age at last installment date is above 65."))
    }
  }

  fun customerIsNotARegisteredDebtor(debtorState: DebtorState): ScoringRule = { loanApplication ->
    when (debtorState) {
      DebtorState.RegisteredDebtor -> LoanApplicationScore.Red(Nel("Customer is registered in debtor registry"))
      DebtorState.NotADebtor -> LoanApplicationScore.Green
    }
  }

  operator fun invoke(debtorState: DebtorState, today: LocalDate): ScoringRule = combine(
    Nel(
      ScoringRules.loanAmountMustBeLowerThanPropertyValue,
      listOf(
        ScoringRules.customerAgeAtTheDateOfLastInstallmentMustBeBelow65(today),
        ScoringRules.customerIsNotARegisteredDebtor(debtorState)))
  )

  fun combine(rules: Nel<ScoringRule>): ScoringRule = { loanApplication ->
    val scores = rules.all.map { rule -> rule(loanApplication) }
    scores.foldLeft<LoanApplicationScore, LoanApplicationScore>(LoanApplicationScore.Green) { allScorees, score ->
      allScorees + score
    }
  }
}
