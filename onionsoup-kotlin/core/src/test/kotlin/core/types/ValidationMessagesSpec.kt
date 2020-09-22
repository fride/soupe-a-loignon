package core.types

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class ValidationMessagesSpec : FunSpec({
  test("isEmpty") {
    ValidationMessages(emptyMap()).isEmpty shouldBe true
  }
})
