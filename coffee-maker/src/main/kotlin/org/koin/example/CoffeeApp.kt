package org.koin.example

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.time.measureDuration
import org.koin.ksp.generated.componentScan

class CoffeeApp : KoinComponent {
    val maker: CoffeeMaker by inject()
}

fun main() {
    startKoin {
        printLogger()
        componentScan(
            modules = listOf(otherModule)
        )
    }

    val coffeeShop = CoffeeApp()
    measureDuration("Got Coffee") {
        coffeeShop.maker.brew()
    }
}

fun measureDuration(msg: String, code: () -> Unit): Double {
    val duration = measureDuration(code)
    println("$msg in $duration ms")
    return duration
}