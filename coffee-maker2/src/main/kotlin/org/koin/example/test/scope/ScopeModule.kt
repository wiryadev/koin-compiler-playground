package org.koin.example.test.scope

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Scope
import org.koin.core.component.KoinScopeComponent

class MyScope
const val MY_SCOPE_SESSION = "MY_SCOPE_SESSION"

@Scope(MyScope::class)
class MyScopedComponent(val myScope : MyScope)

class MyScopedComponent2(val myScope : MyScope)

@Scope(name = MY_SCOPE_SESSION)
class MyScopedSessionComponent

@Module
@ComponentScan
class ScopeModule{

    @Scope(MyScope::class)
    fun myScopedComponent2(myScope : MyScope) = MyScopedComponent2(myScope)
}