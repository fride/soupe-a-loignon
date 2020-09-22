package onionsoup.loanapplication.persistence

import arrow.fx.IO
import arrow.fx.extensions.fx
import onionsoup.loanapplication.UpdateOperator
import onionsoup.loanapplication.persistence.sql.public_.Tables
import org.jooq.Configuration

object JooqUpdateOperator {

  operator fun invoke(configuration: Configuration): UpdateOperator = { operator ->
    configuration.writeTransactionSync { dsl ->
      IO.fx {
        dsl.update(Tables.OPERATOR)
          .set(Tables.OPERATOR.FIRST_NAME, operator.name.firstName)
          .set(Tables.OPERATOR.LAST_NAME, operator.name.secondName)
          .set(Tables.OPERATOR.COMPETENCE_LEVEL, operator.competenceLevel.asEuroCents())
          .where(Tables.OPERATOR.OP_LOGIN.eq(operator.operatorHandle))
          .execute()
      }
    }
  }
}
