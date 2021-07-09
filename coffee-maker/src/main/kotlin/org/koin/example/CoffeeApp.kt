package org.koin.example

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.time.measureDuration
import org.koin.example.coffee.CoffeeAppModule
import org.koin.example.coffee.CoffeeMaker
import org.koin.example.test.ClassModule2
import org.koin.example.test.TestMe2
import org.koin.ksp.generated.*
import org.koin.mp.KoinPlatformTools

class CoffeeApp : KoinComponent {
    val maker: CoffeeMaker by inject()
}

fun main() {
    startKoin {
        printLogger()
        modules(
//            defaultModule,
            CoffeeAppModule().module,
//            ClassModule2().module
        )
    }

    val coffeeShop = CoffeeApp()
    measureDuration("Got Coffee") {
        coffeeShop.maker.brew()
    }

//    KoinPlatformTools.defaultContext().get().get<TestMe2>()
}

fun measureDuration(msg: String, code: () -> Unit): Double {
    val duration = measureDuration(code)
    println("$msg in $duration ms")
    return duration
}