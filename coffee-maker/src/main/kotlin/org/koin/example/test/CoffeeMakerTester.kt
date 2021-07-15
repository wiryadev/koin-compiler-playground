package org.koin.example.test

import org.koin.core.annotation.Single
import org.koin.example.coffee.CoffeeMaker

@Single
class CoffeeMakerTester(val coffeeMaker: CoffeeMaker)