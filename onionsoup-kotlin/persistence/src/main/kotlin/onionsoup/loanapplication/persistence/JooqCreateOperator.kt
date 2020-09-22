package onionsoup.loanapplication.persistence

import arrow.fx.IO
import arrow.fx.extensions.fx
import onionsoup.loanapplication.CreateOperator
import onionsoup.loanapplication.persistence.sql.public_.Tables
import onionsoup.loanapplication.persistence.sql.public_.Tables.OPERATOR
import org.jooq.Configuration

object JooqCreateOperator {

  operator fun invoke(configuration: Configuration) : CreateOperator = {operator ->
    configuration.writeTransactionSync { dsl ->
      IO.fx {
        dsl.insertInto(OPERATOR,
          OPERATOR.OP_LOGIN,
          OPERATOR.FIRST_NAME,
          OPERATOR.LAST_NAME,
          OPERATOR.COMPETENCE_LEVEL)
          .values(
            operator.operatorHandle,
            operator.name.firstName,
            operator.name.secondName,
            operator.competenceLevel.asEuroCents()
          ).execute()
        IO.unit
      }
    }
  }
}
