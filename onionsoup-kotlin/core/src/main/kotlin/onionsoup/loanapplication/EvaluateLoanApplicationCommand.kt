package onionsoup.loanapplication

import core.types.Command

data class EvaluateLoanApplicationCommand (
  val loanApplicationDraftId: String,
  val operatorHandle: OperatorHandle,
  override val requestId: String
) : Command

