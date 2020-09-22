package core.extensions

import java.time.Clock
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

fun Clock.utcNow(): OffsetDateTime = OffsetDateTime.ofInstant(this.instant(), ZoneId.of("UTC"))
fun Clock.today(): LocalDate = LocalDate.ofInstant(this.instant(), ZoneId.of("UTC"))
