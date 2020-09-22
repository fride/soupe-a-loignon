package onionsoup.loanapplication.persistence

import arrow.fx.IO
import arrow.fx.extensions.fx
import onionsoup.loanapplication.DeleteOperator
import onionsoup.loanapplication.persistence.sql.public_.Tables
import org.jooq.Configuration

object JooqDeleteOperator {

  operator fun invoke(configuration: Configuration): DeleteOperator = { id ->
    configuration.writeTransactionSync { dsl ->
      IO.fx {
        dsl.deleteFrom(Tables.OPERATOR)
          .where(Tables.OPERATOR.OP_LOGIN.eq(id))
          .execute()
        unit()
      }
    }
  }
}
