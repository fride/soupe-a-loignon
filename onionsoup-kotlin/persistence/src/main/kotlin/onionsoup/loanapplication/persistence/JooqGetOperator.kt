package onionsoup.loanapplication.persistence

import arrow.fx.IO
import arrow.fx.extensions.fx
import onionsoup.loanapplication.GetOperator
import onionsoup.loanapplication.Name
import onionsoup.loanapplication.Operator
import onionsoup.loanapplication.persistence.sql.public_.Tables.OPERATOR
import org.jooq.Configuration
import java.math.BigDecimal

object JooqGetOperator {

  operator fun invoke(configuration: Configuration): GetOperator = { id ->
    configuration.readOnlyTransaction { dsl ->
      IO.fx {
        dsl.select(OPERATOR.FIRST_NAME, OPERATOR.LAST_NAME, OPERATOR.COMPETENCE_LEVEL)
          .from(OPERATOR)
          .where(OPERATOR.OP_LOGIN.eq(id))
          .fetchOptional()
          .map {
            Operator(
              operatorHandle = id,
              Name(firstName = it[OPERATOR.FIRST_NAME], secondName = it[OPERATOR.LAST_NAME]),
              competenceLevel = it[OPERATOR.COMPETENCE_LEVEL].fromEuroCents() ?: BigDecimal.ZERO
            )
          }.toOption()
      }
    }
  }
}
