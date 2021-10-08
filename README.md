# Koin Compiler - Sandbox

The goal of Koin compiler & Annotations project is to help declare Koin definition in a very fast and intuitive way, and generate all underlying Koin DSL.

## Koin Annotation Processing 🚀

Koin code generation is mostly instant for Koin: just a few lines to generate. It is really easily readable/debuggable. 

Even with ~~1000 of definitions, project compilation has almost no impact. 

Here with such power from Ksp project, we can bring ease even more Kotlin dependency injection. We keep Koin API as it. We just avoid you to write definitions and modules.

### Automatic definition binding

When tagging a component to be defined in Koin, we can easily declare all related supertypes directly:

```kotlin
@Single
class ElectricHeater : Heater 
```

would generate definition:

```kotlin
single { ElectricHeater() } bind Heater::class
```

### Using generated content

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

### Running definitions without any module

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

### Defining a module with a Class

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

### Scanning definitions

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

We can also specify what package to scan in `@ComponentScan` value. Below we scan annotated components in `org.koin.example.test` package:

```kotlin
@Module
@ComponentScan("org.koin.example.test")
class CoffeeAppModule
```

### Unmatched Definitions

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

### Mixing declarations in a Class Module

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

### Multiple Class Modules

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
  modules(
    CoffeeAppModule().module,
    // new module here, with .module generated extension
    OtherModule().module
  )
}
```

## TODO 🚧

Basic Definition Options for Type & Functions: (In Progress)
- Create at start ✅
- Qualifier (@Qualifier) ✅

Class Module ✅
- Component Scan ✅
- Class modules in same packe, but different component scan ✅

- Keywords extension & Dynamic import ✅
- Android Keywords
    - @KoinViewModel ✅
    - @Fragment
    - @Worker

- Generate defaultModule if needed ✅
  
Parameter Injection (@Param) - it.getParam() ✅
- Ctor ✅
- Fun ✅

Property (@Property) - getProperty(key) ✅
- Ctor ✅
- Fun ✅

Scope Structure (@Scope)
- List all scopes structures and prepare for generation ✅
- @Scope on a type T -> generated scoped { T } in given scope  ✅
- except if tagged @Factory, @ViewModel or any kind of factory component

- Clean code 🚧
- Incremental gen 🚧 






