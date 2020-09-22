package onionsoup.loanapplication.persistence

import arrow.fx.IO
import arrow.fx.extensions.fx
import onionsoup.loanapplication.FindOperator
import onionsoup.loanapplication.Name
import onionsoup.loanapplication.Operator
import onionsoup.loanapplication.persistence.sql.public_.Tables.OPERATOR
import org.jooq.Configuration
import java.math.BigDecimal


object JooqFindOperator {
  operator fun invoke(configuration: Configuration): FindOperator = { id ->
    configuration.readOnlyTransaction { dsl ->
      IO.fx {
        dsl.select(OPERATOR.FIRST_NAME, OPERATOR.COMPETENCE_LEVEL, OPERATOR.LAST_NAME)
          .from(OPERATOR)
          .where(OPERATOR.OP_LOGIN.eq(id))
          .fetchOptional()
          .map {
            Operator(
              operatorHandle = id,
              name = Name(it[OPERATOR.FIRST_NAME],it[OPERATOR.LAST_NAME]),
              competenceLevel = it[OPERATOR.COMPETENCE_LEVEL].fromEuroCents() ?: BigDecimal.ZERO
            )
          }.toOption()
      }
    }
  }
}
