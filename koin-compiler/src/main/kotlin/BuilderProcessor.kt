import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Single
import java.io.OutputStream

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

typealias ModuleIndex = Pair<String, KoinMetaData.Module>

class BuilderProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {

    lateinit var moduleMap: Map<String, KoinMetaData.Module>

    override fun process(resolver: Resolver): List<KSAnnotated> {
//        val ret = singleComponents.filter { !it.validate() }.toList()

        val defaultModule = KoinMetaData.Module(
            packageName = "org.koin.ksp.generated",
            fieldName = "defaultModule"
        )
        scanMetaData(resolver,defaultModule)
        generate(defaultModule)
        return emptyList()
    }

    private fun scanMetaData(resolver: Resolver, defaultModule: KoinMetaData.Module) {
        logger.warn("scan modules ...")

        moduleMap = resolver.getSymbolsWithAnnotation(ComponentScan::class.qualifiedName!!)
            .filter { it is KSPropertyDeclaration && it.validate() }
            .map {
                val declaration = (it as KSPropertyDeclaration)
                logger.warn("module -> $it", it)
                val modulePackage = declaration.containingFile?.packageName?.asString() ?: ""
                val name = "$it"
                val moduleMetadata = KoinMetaData.Module(
                    packageName = modulePackage,
                    fieldName = name
                )
                ModuleIndex(modulePackage, moduleMetadata)
            }.toMap()

        logger.warn("scan definitions ...")
        resolver.getSymbolsWithAnnotation(Single::class.qualifiedName!!)
            .filter { it is KSClassDeclaration && it.validate() }
            .map {
                logger.warn("single -> $it", it)
                val ksClassDeclaration = (it as KSClassDeclaration)
                val packageName = ksClassDeclaration.containingFile!!.packageName.asString()
                val className = ksClassDeclaration.simpleName.asString()
                val definition = KoinMetaData.Definition.Single(
                    packageName = packageName,
                    className = className,
                    constructorParameters = ksClassDeclaration.primaryConstructor?.parameters?.map { KoinMetaData.ConstructorParameter() }
                        ?: emptyList(),
                    bindings = ksClassDeclaration.superTypes.toList()
                )
                addToModule(definition,defaultModule)
            }.toList()
    }

    private fun generate(defaultModule: KoinMetaData.Module) {
        logger.warn("generate ...")
        moduleMap.values.forEachIndexed { index, module ->
            generateModule(module)
            if (index == moduleMap.values.size -1){
                generateModule(defaultModule)
            }
        }
    }

    private fun generateModule(module: KoinMetaData.Module) {
        logger.warn("generate $module")
        val file = codeGenerator.createNewFile(
            Dependencies.ALL_FILES,
            "org.koin.ksp.generated",
            "Default"
        )
        file.appendText("\n// module - ${module.packageName} : $module")

        module.definitions.forEach { def ->
            logger.warn("generate $def")
            val file = codeGenerator.createNewFile(
                Dependencies.ALL_FILES,
                "org.koin.ksp.generated",
                "Default"
            )
            file.appendText("\n// def - ${def.packageName} : ${def.className}")
        }
    }

    private fun addToModule(definition: KoinMetaData.Definition, defaultModule: KoinMetaData.Module) {
        val moduleKey = moduleMap.keys.firstOrNull { definition.packageName.contains(it) }
        val module = moduleMap[moduleKey] ?: defaultModule
        module.definitions.add(definition)
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

}

class BuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return BuilderProcessor(environment.codeGenerator, environment.logger)
    }
}