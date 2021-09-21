package generator

val DEFAULT_MODULE_HEADER = """
        package org.koin.ksp.generated
    
        import org.koin.core.KoinApplication
        import org.koin.core.module.Module
        import org.koin.core.qualifier.StringQualifier
        import org.koin.dsl.module
        import org.koin.dsl.bind
        import org.koin.dsl.binds
        
    """.trimIndent()

val DEFAULT_MODULE_FUNCTION = """
        fun KoinApplication.defaultModule() = modules(defaultModule)
        val defaultModule = module {
    """.trimIndent()

val DEFAULT_MODULE_FOOTER = """
    
        }
    """.trimIndent()

val MODULE_HEADER = """
            package org.koin.ksp.generated
            import org.koin.dsl.*
            import org.koin.core.qualifier.StringQualifier
            
        """.trimIndent()