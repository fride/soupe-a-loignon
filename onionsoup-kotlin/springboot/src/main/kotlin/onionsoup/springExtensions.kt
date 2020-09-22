package onionsoup

import arrow.fx.IO
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

fun <A> IO<A>.readTransaction(tx: PlatformTransactionManager): A = TransactionTemplate(tx)
  .apply {
    isReadOnly = true
    isolationLevel = TransactionDefinition.ISOLATION_READ_COMMITTED
    propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
  }.execute { this.unsafeRunSync() }!!
