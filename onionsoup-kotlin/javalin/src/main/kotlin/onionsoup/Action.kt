package onionsoup

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.fx.IO
import core.extensions.getOrRaiseIoError
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.NotFoundResponse
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.UUID

/**
 * An Action is a handler for a http command. It either invokes a use case
 * or a query. As use cases and queries use IO actions the handler
 * itself is based on IO actions.
 */
class Action(val action: (Context) -> IO<Unit>) : Handler {

  override fun handle(ctx: Context) {
    val start = System.nanoTime()
    val requestId = UUID.randomUUID().toString()
      ctx.attribute("requestId", requestId)
    try {
      LOG.info("[uri: ${ctx.url()} method: ${ctx.method()} requestId: $requestId] called")
      this.action(ctx).unsafeRunSync()
      val duration = Duration.ofNanos(System.nanoTime() - start)
      LOG.info("[uri: ${ctx.url()} method: ${ctx.method()} requestId: $requestId] finished in ${duration.toMillis()}ms.")
    } catch (ex: Exception) {
      val duration = Duration.ofNanos(System.nanoTime() - start)
      LOG.error("[uri: ${ctx.url()} method: ${ctx.method()} requestId: $requestId] failed in ${duration.toMillis()}ms.", ex)
      throw InternalServerErrorResponse("$requestId : action failed")
    }
  }

  companion object {

    private val LOG = LoggerFactory.getLogger(Action::class.java)

    fun Context.requestId(): String = this.let {
      when (val rid: Option<String> = Option.fromNullable(this.attribute<String>("rid"))) {
        is None -> {
          val newRid = this.queryParam("rid") ?: UUID.randomUUID().toString()
          this.attribute("rid", newRid)
          newRid
        }
        is Some -> rid.t
      }
    }

    fun sync(action: (Context) -> IO<Unit>): Handler = Action(action)

    fun redirectTo(action: (Context) -> IO<String>): Action = Action { context ->
      action(context).map { location -> context.redirect(location) }
    }

    fun template(action: (Context) -> IO<Pair<String, Map<String, Any?>>>): Handler = Action { context ->
      val templateAndParams = action(context)
      templateAndParams.map {
        context.render(it.first, it.second)
        Unit
      }
    }

    fun <A> IO<Option<A>>.getOr404() = this.getOrRaiseIoError { NotFoundResponse() }
  }
}
