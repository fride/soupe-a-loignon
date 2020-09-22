package onionsoup.loanapplication

import arrow.fx.IO
import onionsoup.loanapplication.persistence.sql.public_.Tables
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_SCORE
import onionsoup.loanapplication.persistence.sql.public_.Tables.LOAN_APPLICATION_STATE
import onionsoup.loanapplication.persistence.writeTransactionSync
import org.jooq.Configuration

object JooqPersistLoanApplicationScored {

  operator fun invoke(configuration: Configuration): PersistLoanApplicationScored = { update ->
    configuration.writeTransactionSync { dsl ->
      IO {
        val comments = when (val score = update.score) {
          is LoanApplicationScore.Red -> score.reasons.all.toTypedArray()
          is LoanApplicationScore.Green -> emptyArray()
        }
        dsl.insertInto(LOAN_APPLICATION_SCORE,
          LOAN_APPLICATION_SCORE.LOAN_APPLICATION_ID,
          LOAN_APPLICATION_SCORE.SCORE,
          LOAN_APPLICATION_SCORE.COMMENTS)
          .values(
            update.aggregateId,
            when (update.score) {
              is LoanApplicationScore.Red -> "red"
              is LoanApplicationScore.Green -> "green"
            },
            comments
          ).execute()
        dsl.update(LOAN_APPLICATION_STATE)
          .set(LOAN_APPLICATION_STATE.CHECKED_AT, update.occurredAt.toLocalDateTime())
          .where(LOAN_APPLICATION_STATE.LOAN_APPLICATION_ID.eq(update.aggregateId))
          .execute()
        Unit
      }
    }
  }
}
