package onionsoup

import io.javalin.core.security.AccessManager
import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.UnauthorizedResponse

enum class OnionRole : Role {
  Anyone, Visitor, Operator, Admin
}

data class OnionUser(val login: String) {
  companion object{
    fun Context.onionUser() : OnionUser =
      this.attribute<String>("userId")?.let{
        OnionUser(it)
      } ?: throw UnauthorizedResponse()
  }
}
object OnionAccessManger : AccessManager {

  val userMap = mapOf(
    "test1" to setOf(OnionRole.Operator),
    "visitor" to setOf(OnionRole.Visitor),
    "gerald" to setOf(OnionRole.Operator, OnionRole.Admin)
  )

  private fun String?.userRole(): Set<OnionRole> {
    return userMap[this] ?: emptySet()
  }

  override fun manage(handler: Handler, ctx: Context, permittedRoles: MutableSet<Role>) {
    val userId = ctx.cookie("onion-person")
    val role = userId.userRole()
    ctx.attribute("userId", userId)
    when {
      permittedRoles.contains(OnionRole.Anyone) -> handler.handle(ctx)
      else -> {
        if (role.any { it in permittedRoles }) {
          handler.handle(ctx)
        } else {
          ctx.redirect("/login")
        }
      }
    }
  }
}
