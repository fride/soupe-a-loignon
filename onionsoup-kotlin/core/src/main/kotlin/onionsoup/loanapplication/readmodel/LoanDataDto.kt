package onionsoup.loanapplication.readmodel

import core.types.ValidationMessages
import onionsoup.loanapplication.LoanDraft
import java.math.BigDecimal

interface LoanData {
  val amount: BigDecimal?
  val duration: Int?
  val interestRate: BigDecimal?
  val _messages: ValidationMessages
}

data class LoanDataDto(
  override val amount: BigDecimal?,
  override val duration: Int?,
  override val interestRate: BigDecimal?,
  override val _messages: ValidationMessages
) : LoanData {

  companion object {
    fun emptyLoanData(): LoanDataDto = LoanDataDto(
      null,
      null,
      null,
      _messages = ValidationMessages.empty()
    )

    fun LoanDraft.toLoanData(): LoanDataDto = LoanDataDto(
      amount = this.amount,
      duration = this.duration,
      interestRate = this.interestRate,
      _messages = ValidationMessages.empty()
    )
  }
}
