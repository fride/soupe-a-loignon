package onionsoup.loanapplication

import onionsoup.loanapplication.readmodel.FindLoanApplicationDetails
import onionsoup.loanapplication.readmodel.LoadDashboard

interface LoanApplicationModule {
  val newLoanApplicationUseCase: NewLoanApplicationUseCase
  val updateLoanApplicationUseCase: UpdateLoanApplicationUseCase
  val submitLoanApplicationDraftUseCase: SubmitLoanApplicationDraftUseCase
  val scoreLoanApplicationUseCase : ScoreLoanApplicationUseCase

  // queries
  val findLoanApplicationDetails: FindLoanApplicationDetails
  val loadDashboard: LoadDashboard
  val getOperator : GetOperator
  val getAllOperators : GetAllOperators


  // example for crud actions. Sometimes
  // this is the way to go ;)
  val createOperator : CreateOperator
  val updateOperator : UpdateOperator
  val deleteOperator : DeleteOperator
}
