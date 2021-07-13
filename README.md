# Koin Compiler - Sandbox

The goal of Koin compiler & Annotations project is to help declare Koin definition in a very fast and intuitive way, and generate all underlying Koin DSL.

## Current Annotation Processing - Use Cases

### Using Koin generated content

The only thing to setup in particular is the `org.koin.ksp.generated.*` import as follow, to be able to use genrated extensions:

```kotlin
import org.koin.ksp.generated.*

fun main() {
    startKoin {
        printLogger()
        // ... generated features
    }
    // ...
}
```

### quickstart: just annotated definitions

We need to use a default module with `defaultModule()` extension:

```kotlin
import org.koin.ksp.generated.*

fun main() {
    startKoin {
        printLogger()
        // generated default module
        defaultModule()
    }
    // ...
}
```

tag with `@Single` any components:

```kotlin

@Single
class CoffeeMaker(private val pump: Pump, private val heater: Heater)

@Single
class Thermosiphon(private val heater: Heater) : Pump

@Single
class ElectricHeater : Heater 
```

What is generated:
```kotlin
package org.koin.ksp.generated
// imports ...

fun KoinApplication.defaultModule() = modules(defaultModule)
val defaultModule = module {
    // definitions here ...
}
```

### `@Single` generating definition and bindings

When annotating a component with `@Single`, it will generates definition for given class instance and bind all extended types:

```kotlin
@Single
class Thermosiphon(private val heater: Heater) : Pump
```

what is generated:

```kotlin
single { org.koin.example.coffee.pump.Thermosiphon(get()) } bind(org.koin.example.coffee.pump.Pump::class)
```

later we can specify `binds` property in the annotation, to specify the desired bound type for the definition.

### Class Module & declare definitions

We want to use a `CoffeeAppModule` module class. The `.module` extension on `CoffeeAppModule` class will be generated:

```kotlin
import org.koin.ksp.generated.*

fun main() {
    startKoin {
        printLogger()
        modules(
        // generated .module extension on module class
          CoffeeAppModule().module
        )
    }
    // ...
}
```

Let's define a class module with annotations on functions:

```kotlin
@Module
class CoffeeAppModule {

  @Single
  fun heater() : Heater = ElectricHeater()

  @Single
  fun pump(heater: Heater) : Pump = Thermosiphon(heater)

  @Single
  fun coffeeMaker(heater: Heater, pump: Pump) = CoffeeMaker(pump, heater)
}
```

What is generated:
```kotlin
package org.koin.ksp.generated
// imports

val CoffeeAppModuleModule = module {
  // generated module instance
  val moduleInstance = org.koin.example.coffee.CoffeeAppModule()
  // definitions using functions here ...
}
val org.koin.example.coffee.CoffeeAppModule.module : org.koin.core.module.Module get() = CoffeeAppModuleModule
```

### Class Module & Scan all definitions

Rather than defining each component, we can allow a module to scan definitions for current package and sub packages:

```kotlin
import org.koin.ksp.generated.*

fun main() {
    startKoin {
        printLogger()
        modules(
        // generated .module extension on moduel class
          CoffeeAppModule().module
        )
    }
    // ...
}
```

Let's define a class module, use `@ComponentScan` to scan definitions for a given module, `@Single` on components:

```kotlin
@Module
@ComponentScan
class CoffeeAppModule
```

What is generated:
```kotlin
package org.koin.ksp.generated
// imports

val CoffeeAppModuleModule = module {
  val moduleInstance = org.koin.example.coffee.CoffeeAppModule()
  // definitions here ...
}
val org.koin.example.coffee.CoffeeAppModule.module : org.koin.core.module.Module get() = CoffeeAppModuleModule
```

### Unmatched definitions in default module

In case of using `@ComponentScan`, if any definition is tagged but not associated to a declared module, this definition will fallback into the `defaultModule`

```kotlin
import org.koin.ksp.generated.*

fun main() {
    startKoin {
        printLogger()
        modules(
          // generated default module
          defaultModule,
        // generated .module extension on module class
          CoffeeAppModule().module
        )
    }
    // ...
}
```

### Class Module, mixing declared & scanned definition

As with previous case:

```kotlin
import org.koin.ksp.generated.*

fun main() {
    startKoin {
        printLogger()
        modules(
        // generated .module extension on moduel class
          CoffeeAppModule().module
        )
    }
    // ...
}
```

We can combine `@ComponentScan` & annotated functions definition:

```kotlin
@Module
@ComponentScan
class CoffeeAppModule {

  @Single
  fun coffeeMaker(heater: Heater, pump: Pump) = CoffeeMaker(pump, heater)
}
```

We keep `@Single` annotations on needed components:

```kotlin

@Single
class CoffeeMaker(private val pump: Pump, private val heater: Heater)

@Single
class Thermosiphon(private val heater: Heater) : Pump

class ElectricHeater : Heater 
```

Generated content will add all definitions for module generation, like previous case.

### Multiple Modules

Any class module tagged with `@Module` will be generated. Just import the module like follow:

```kotlin
@Module
@ComponentScan
class OtherModule
```

Just use it with the `.module` generated extension:

```kotlin
import org.koin.ksp.generated.*

startKoin {
  printLogger()
  modules(
    CoffeeAppModule().module,
    // new module here, with .module generated extension
    OtherModule().module
  )
}
```

## TODO

Basic Definition Creation:
- Definition
    - Binds
    - Create at start 
- Qualifier / String Qualifier (@Qualifier)
  - Ctor
  - Fun
- Generic for other keywords with factory (help for later Android)
- Android Keywords
    - @ViewModel
    - @Fragment
    - @Worker
  
Parameter Injection (@Param)
- Ctor
- Fun

Property (@Property)
- getProperty(key) <T>

Scope Structure (@Scope)
- @Scope on a type
- @ScopedIn?
- @ScopedIn for factory definitions? (visibility problem)

Other:
- clean up warn message to log info
- better core compiler code/refacto







