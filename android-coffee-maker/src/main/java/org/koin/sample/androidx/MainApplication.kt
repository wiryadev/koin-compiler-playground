package org.koin.sample.androidx

import android.app.Application
import org.koin.core.context.startKoin
import org.koin.example.di.CoffeeAppModule
import org.koin.ksp.generated.*

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(
                MyModule().module,
                CoffeeAppModule().module
            )
        }
    }
}