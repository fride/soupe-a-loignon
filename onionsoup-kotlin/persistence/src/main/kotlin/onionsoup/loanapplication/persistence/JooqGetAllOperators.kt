package onionsoup.loanapplication.persistence

import arrow.fx.IO
import arrow.fx.extensions.fx
import onionsoup.loanapplication.GetAllOperators
import onionsoup.loanapplication.GetOperator
import onionsoup.loanapplication.Name
import onionsoup.loanapplication.Operator
import onionsoup.loanapplication.persistence.sql.public_.Tables.OPERATOR
import org.jooq.Configuration
import java.math.BigDecimal

object JooqGetAllOperators {

  operator fun invoke(configuration: Configuration): GetAllOperators = { offset, pagesize ->
    configuration.readOnlyTransaction { dsl ->
      IO.fx {
        dsl.select(OPERATOR.FIRST_NAME, OPERATOR.LAST_NAME, OPERATOR.COMPETENCE_LEVEL, OPERATOR.OP_LOGIN)
          .from(OPERATOR)
          .offset(offset)
          .limit(pagesize)
          .fetch()
          .map {
            Operator(
              operatorHandle = it[OPERATOR.OP_LOGIN],
              Name(firstName = it[OPERATOR.FIRST_NAME], secondName = it[OPERATOR.LAST_NAME]),
              competenceLevel = it[OPERATOR.COMPETENCE_LEVEL].fromEuroCents() ?: BigDecimal.ZERO
            )
          }
      }
    }
  }
}
