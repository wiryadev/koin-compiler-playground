package org.koin.example.test.ext

import org.koin.core.annotation.*


class TestComponent
class TestComponentConsumer(val tc : TestComponent, val id : String)

@Single
class TestComponentConsumer2(@Qualifier("tc") val tc : TestComponent, @InjectedParam val id : String)

@Single
class PropertyComponent(@Property("prop_id") val id : String)

class PropertyComponent2(val id : String)

@Module
@ComponentScan
class ExternalModule {

    @Single(createdAtStart = true)
    @Qualifier("tc")
    fun testComponent() = TestComponent()

    @Single
    fun testComponentConsumer(@Qualifier("tc") tc : TestComponent, @InjectedParam id : String) = TestComponentConsumer(tc,id)

    @Single
    fun propertyComponent2(@Property("prop_id") id : String) = PropertyComponent2(id)
}