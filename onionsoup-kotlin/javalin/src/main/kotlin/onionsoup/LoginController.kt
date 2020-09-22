package onionsoup

import arrow.core.Option
import arrow.fx.IO
import arrow.fx.extensions.fx

object LoginController {

  fun get() = Action.sync { ctx ->
    ctx.html("""
      |<html>
      |<body>
      |<form method="post">
      |<input type="text" name="userName">
      |<input type="submit">Log in</input>
      |</form>
      |</body>
      |</html>
    """.trimMargin())
    IO.unit
  }

  fun post() = Action.redirectTo { ctx ->
    IO.fx {
      Option.fromNullable(ctx.formParam("userName"))
        .filter { OnionAccessManger.userMap.containsKey(it) }
        .fold(
          { IO.just("/login") },
          { userName ->
            ctx.cookie("onion-person", userName, Int.MAX_VALUE)
            IO.just("/loan_application")
          }
        ).bind()
    }
  }
}
