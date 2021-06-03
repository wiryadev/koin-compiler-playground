import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Single
import java.io.OutputStream

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

class BuilderProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val singleComponents = resolver.getSymbolsWithAnnotation(Single::class.qualifiedName!!)
        val ret = singleComponents.filter { !it.validate() }.toList()
        val all = singleComponents.filter { it is KSClassDeclaration && it.validate() }
        val count = all.count()
        all.forEachIndexed { index, ksAnnotated ->
            ksAnnotated.accept(
                SingleBuilderVisitor(
                    index == 0,
                    index == count - 1
                ), Unit
            )
        }

        val componentScans = resolver.getSymbolsWithAnnotation(ComponentScan::class.qualifiedName!!)
        componentScans.filter { it is KSPropertyDeclaration && it.validate() }
            .forEach {

                val declaration = (it as KSPropertyDeclaration)
                logger.warn("declaration -> $it",it)
                val modulePackage = declaration.containingFile?.packageName?.asString() ?: ""
                val name = "$it"
                val file = codeGenerator.createNewFile(
                    Dependencies.ALL_FILES,
                    "org.koin.ksp.generated",
                    "Default"
                )
                file.appendText("// $modulePackage.$name.apply { }")
            }

        return emptyList()
    }

    inner class SingleBuilderVisitor(val isFirst: Boolean, val isLast: Boolean) : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            classDeclaration.primaryConstructor!!.accept(this, data)
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val ksClassDeclaration = function.parentDeclaration as KSClassDeclaration
            val packageName = ksClassDeclaration.containingFile!!.packageName.asString()
            val className = ksClassDeclaration.simpleName.asString()
            val file = codeGenerator.createNewFile(
                Dependencies.ALL_FILES,
                "org.koin.ksp.generated",
                "Default"
            ) //TODO Incremental here?
            val paramsCount = ksClassDeclaration.primaryConstructor?.parameters?.count() ?: 0
            val ctor = if (paramsCount > 0) (1..paramsCount).joinToString(
                ",",
                prefix = "(",
                postfix = ")"
            ) { "get()" } else "()"
            val interfaces = ksClassDeclaration.superTypes.map {
                val ksType = it.resolve()
                ksType.declaration.packageName.asString() + "." + ksType.declaration.simpleName.asString()
            }.joinToString { "bind $it::class" }
            if (isFirst) {
                file.appendText(
                    """
                    package org.koin.ksp.generated

                    import org.koin.core.KoinApplication
                    import org.koin.core.module.Module
                    import org.koin.dsl.module
                    import org.koin.dsl.bind

                    fun KoinApplication.allModules(modules : List<Module> = emptyList(), useDefaultModule : Boolean = true) {
                        val defaultModule = module {
                """.trimIndent()
                )
                file.appendText("\n")
            }
            file.appendText("\tsingle { $packageName.$className$ctor } $interfaces\n")
            if (isLast) {
                file.appendText(
                    """
                        }
                        modules(defaultModule+modules)
                    }
                """.trimIndent()
                )
            }
        }
    }

//    inner class ModuleBuilderVisitor : KSVisitorVoid() {
////        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
////            logger.error("visitPropertyDeclaration",property)
////            property.accept(this, data)
////        }
//
//        override fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: Unit) {
//            logger.error("visitDeclarationContainer",declarationContainer)
//        }
//
//        override fun visitDeclaration(declaration: KSDeclaration, data: Unit) {
//            logger.error("visitDeclaration",declaration)
//            val ksClassDeclaration = declaration.parentDeclaration as KSPropertyDeclaration
//            val packageName = ksClassDeclaration.containingFile!!.packageName.asString()
//            val name = ksClassDeclaration.simpleName.asString()
//            val file = codeGenerator.createNewFile(
//                Dependencies.ALL_FILES,
//                "org.koin.ksp.generated",
//                "Default"
//            )
//            file.appendText("//$name")
//        }
//
//        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
//
//        }
//    }

}

class BuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return BuilderProcessor(env.codeGenerator, env.logger)
    }
}