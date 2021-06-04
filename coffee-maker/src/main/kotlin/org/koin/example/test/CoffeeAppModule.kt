package org.koin.example.test

import org.koin.core.annotation.ComponentScan
import org.koin.dsl.module


// val coffeeAppModule = module {
//     single<CoffeeMaker>()
//     single<Thermosiphon>() bind Pump::class
//     single<ElectricHeater>() bind Heater::class
// }

@ComponentScan
val otherModule = module { }