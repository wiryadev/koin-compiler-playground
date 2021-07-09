import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSDeclaration
import java.io.OutputStream

class KoinCodeGenerator(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) {

    val defaultModuleHeader = """
        package org.koin.ksp.generated
    
        import org.koin.core.KoinApplication
        import org.koin.core.module.Module
        import org.koin.dsl.module
        import org.koin.dsl.bind
        import org.koin.dsl.binds
        
        fun KoinApplication.defaultModule() = modules(defaultModule)
        val defaultModule = module {
    """.trimIndent()

    val defaultModuleFooter = """
        }
    """.trimIndent()

    fun generateModules(
        moduleMap: Map<String, KoinMetaData.Module>,
        defaultModule: KoinMetaData.Module
    ) {
        logger.warn("generate ${moduleMap.size} modules ...")
        moduleMap.values.forEachIndexed { index, module ->
            if (index == 0) {
                val file = getDefaultFile()
                file.appendText(defaultModuleHeader)
            }
            generateModule(module)
            if (index == moduleMap.values.size - 1) {
                generateModule(defaultModule)
                val file = getDefaultFile()
                file.appendText("\n" + defaultModuleFooter)
            }
        }
    }

    private fun generateModule(module: KoinMetaData.Module) {
        logger.warn("generate $module - ${module.type}")
        getDefaultFile().let { defaultFile ->
            if (module.definitions.isNotEmpty()) {
                when (module.type) {
                    KoinMetaData.ModuleType.FIELD -> defaultFile.generateFieldModule(module)
                    KoinMetaData.ModuleType.CLASS -> generateClassModule(module)
                }
            } else {
                logger.warn("no definition for $module")
            }
        }
    }

    private fun generateClassModule(module: KoinMetaData.Module) {
        val classFile = getFile(fileName = "${module.name}Gen")
        classFile.appendText(
            """
            package org.koin.ksp.generated
            import org.koin.dsl.module
            import org.koin.dsl.bind
        """.trimIndent()
        )
        val generatedField = "${module.name}Module"
        val classModule = "${module.packageName}.${module.name}"
        classFile.appendText("\nval $generatedField = module {")
        classFile.appendText("\n\t\t\t\tval moduleInstance = $classModule()")
        // Definitions here
        module.definitions.filterIsInstance<KoinMetaData.Definition.FunctionDeclarationDefinition>().forEach { def ->
            classFile.generateFunctionDeclarationDefinition(def)
        }
        module.definitions.filterIsInstance<KoinMetaData.Definition.ClassDeclarationDefinition>().forEach { def ->
            classFile.generateClassDeclarationDefinition(def)
        }
        classFile.appendText("\n}")
        classFile.appendText("\nval $classModule.module : org.koin.core.module.Module get() = $generatedField")
        classFile.flush()
        classFile.close()
    }

    private fun OutputStream.generateFunctionDeclarationDefinition(def: KoinMetaData.Definition.FunctionDeclarationDefinition) {
        val ctor = generateConstructor(def.parameters)
        appendText("\n\t\t\t\t${def.keyword} {  moduleInstance.${def.functionName}$ctor }")
    }

    private fun OutputStream.generateFieldModule(module: KoinMetaData.Module) {
        module.definitions.filterIsInstance<KoinMetaData.Definition.ClassDeclarationDefinition>().forEach { def ->
            logger.warn("generate $def")
            generateClassDeclarationDefinition(def)
        }
    }

    fun generateDefaultDefinitions(
        definitions: List<KoinMetaData.Definition>
    ) {
        logger.warn("generate default module")
        definitions.forEachIndexed { index, def ->
            if (index == 0) {
                getDefaultFile().apply {
                    appendText(defaultModuleHeader)
                    appendText("\n\t\t" + defaultModuleFooter)
                }
            }
            logger.warn("generate $def")
            if (def is KoinMetaData.Definition.ClassDeclarationDefinition) {
                getDefaultFile().generateClassDeclarationDefinition(def)
            }
            if (index == definitions.size - 1) {
                getDefaultFile().apply {
                    appendText("\n\t\t}\n")
                    appendText(defaultModuleFooter)
                }
            }
        }
    }

    private fun OutputStream.generateClassDeclarationDefinition(def: KoinMetaData.Definition.ClassDeclarationDefinition) {
        val param =
            if (def.constructorParameters.filter { it.type == KoinMetaData.ConstructorParameterType.PARAMETER_INJECT }
                    .isEmpty()) "" else " params ->"
        val ctor = generateConstructor(def.constructorParameters)
        val binds = generateBindings(def.bindings)
        appendText("\n\t\t\t\t${def.keyword} { $param${def.packageName}.${def.className}$ctor } $binds")
    }

    private fun generateBindings(bindings: List<KSDeclaration>): String {
        return when {
            bindings.isEmpty() -> ""
            bindings.size == 1 -> "bind(${generateBinding(bindings.first())})"
            else -> bindings.joinToString(prefix = "binds(", separator = ",", postfix = ")") { generateBinding(it) }
        }
    }

    private fun generateBinding(declaration: KSDeclaration): String {
        val packageName = declaration.containingFile!!.packageName.asString()
        val className = declaration.simpleName.asString()
        return "$packageName.$className::class"
    }

    private fun generateConstructor(constructorParameters: List<KoinMetaData.ConstructorParameter>): String {
        return constructorParameters.joinToString(prefix = "(", separator = ",", postfix = ")") {
            if (it.type == KoinMetaData.ConstructorParameterType.DEPENDENCY) "get()" else "params.get()"
        }
    }

    private fun getDefaultFile() = codeGenerator.createNewFile(
        Dependencies.ALL_FILES,
        "org.koin.ksp.generated",
        "Default"
    )

    private fun getFile(packageName: String = "org.koin.ksp.generated", fileName: String) = codeGenerator.createNewFile(
        Dependencies.ALL_FILES,
        packageName,
        fileName
    )
}