package me.kavishdevar.librepods.utils

import io.github.libxposed.api.XposedModule
import java.lang.reflect.Member

/** Installs an upstream-style interceptor through the pinned API 100 callback. */
fun XposedModule.intercept(member: Member, body: (Api100Interception.Chain) -> Any?) {
    Api100Interception.install(this, member) { chain -> body(chain) }
}
