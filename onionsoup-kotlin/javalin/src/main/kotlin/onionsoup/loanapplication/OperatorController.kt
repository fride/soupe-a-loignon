package onionsoup.loanapplication

import arrow.fx.IO
import arrow.fx.extensions.fx
import io.javalin.http.Context
import onionsoup.Action
import onionsoup.Action.Companion.getOr404
import java.math.BigDecimal

class OperatorController(private val createOperator: CreateOperator,
                         private val updateOperator: UpdateOperator,
                         private val deleteOperator: DeleteOperator,
                         private val getOperator: GetOperator,
                         private val getAllOperators: GetAllOperators) {

  val new = Action.template { ctx ->
    IO.just("templates/operator/new.peb.html"
      to mapOf(
      "cancel" to "/admin/operator",
      "isNew" to true,
      "action" to  "/admin/operator"))
  }

  val get = Action.template { ctx ->
    IO.fx {
      val operator = !getOperator(ctx.pathParam("operator-id")).getOr404()
      "templates/operator/view.peb.html" to
        mapOf("operator" to operator,
        "edit" to "/admin/operator/${operator.operatorHandle}/edit")
    }
  }

  val edit = Action.template { ctx ->
    IO.fx {
      val operator = !getOperator(ctx.pathParam("operator-id")).getOr404()
      "templates/operator/edit.peb.html" to mapOf(
        "operator" to operator,
        "cancel" to "/admin/operator/${operator.operatorHandle}",
        "update" to "/admin/operator/${operator.operatorHandle}")
    }
  }

  val getAll = Action.template { ctx ->
    IO.fx {
      val operators = !getAllOperators(0, 200)
      "templates/operator/index.peb.html" to
        mapOf("operators" to operators,
        "new" to "/admin/operator/new")
    }
  }

  val create = Action.redirectTo { ctx ->
    IO.fx {
      val operator = ctx.operatorFromForm()
      !createOperator(operator).map {
        "/admin/operator"
      }
    }
  }

  val update = Action.redirectTo { ctx ->
    IO.fx {
      val operator = ctx.operatorFromForm()
      !updateOperator(operator).map {
        "/admin/operator/${operator.operatorHandle}"
      }
    }
  }

  val delete = Action.sync { ctx ->
    IO.fx {
      !deleteOperator(ctx.pathParam("operator-id"))
      unit()
    }
  }

  companion object {
    fun Context.operatorFromForm(): Operator {
      val handle = this.formParam("handle")!!
      val firstName = this.formParam("firstName")!!
      val lastName = this.formParam("lastName")!!
      val competenceLevel = this.formParam("competenceLevel", BigDecimal::class.java).get()
      return Operator(
        operatorHandle = handle,
        name = Name(firstName, lastName),
        competenceLevel = competenceLevel
      )
    }
  }
}
