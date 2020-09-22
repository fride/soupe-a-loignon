package core.types

import arrow.fx.IO
import java.time.OffsetDateTime

typealias AggregateId = String

interface ModelUpdate {
  val aggregateId: AggregateId
  val occurredAt: OffsetDateTime
}

// update the model in one single transaction.
typealias ModelUpdater<T> = (T) -> IO<Unit>
