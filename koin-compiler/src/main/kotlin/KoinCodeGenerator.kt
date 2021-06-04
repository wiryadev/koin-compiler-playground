import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSDeclaration
import java.io.OutputStream

class KoinCodeGenerator(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) {

    val allModulesHeader = """
        package org.koin.ksp.generated
    
        import org.koin.core.KoinApplication
        import org.koin.core.module.Module
        import org.koin.dsl.module
        import org.koin.dsl.bind
        import org.koin.dsl.binds
        
        val defaultModule = module {}
    
        fun KoinApplication.componentScan(modules : List<Module> = listOf(), useDefaultModule : Boolean = true) {
    """.trimIndent()

    val allModulesFooter = """
            if (useDefaultModule){
                modules(defaultModule+modules)
            } else {
                modules(modules)
            }
        }
    """.trimIndent()

    val defaultModuleApply = """
            defaultModule.apply {
    """.trimIndent()

    fun generateModules(
        moduleMap: Map<String, KoinMetaData.Module>,
        defaultModule: KoinMetaData.Module
    ) {
        logger.warn("generate ...")
        moduleMap.values.forEachIndexed { index, module ->
            if (index == 0) {
                val file = getDefaultFile()
                file.appendText(allModulesHeader)
            }
            generateModule(module)
            if (index == moduleMap.values.size - 1) {
                generateModule(defaultModule)
                val file = getDefaultFile()
                file.appendText("\n" + allModulesFooter)
            }
        }
    }

    fun generateDefinitions(
        definitions: List<KoinMetaData.Definition>
    ) {
        logger.warn("generate ...")
        definitions.forEachIndexed { index,  def ->
            if (index == 0){
                getDefaultFile().apply {
                    appendText(allModulesHeader)
                    appendText("\n\t\t"+defaultModuleApply)
                }
            }
            logger.warn("generate $def")
            generateDefinition(def)
            if (index == definitions.size -1){
                getDefaultFile().apply {
                    appendText("\n\t\t}\n")
                    appendText(allModulesFooter)
                }
            }
        }
    }

    private fun generateModule(module: KoinMetaData.Module) {
        logger.warn("generate $module")
        getDefaultFile().apply {
            if (module.definitions.isNotEmpty()) {
                appendText("\n\t ${module.packageName.dotPackage()}${module.fieldName}.apply {")
                module.definitions.forEach { def ->
                    logger.warn("generate $def")
                    generateDefinition(def)
                }
                appendText("\n\t}")
            } else {
                logger.warn("no definition for $module")
            }
        }
    }

    private fun generateDefinition(def: KoinMetaData.Definition) {
        getDefaultFile().apply {
            val param =
                if (def.constructorParameters.filter { it.type == KoinMetaData.ConstructorParameterType.PARAMETER_INJECT }
                        .isEmpty()) "" else " params ->"
            val ctor = generateConstructor(def.constructorParameters)
            val binds = generateBindings(def.bindings)
            appendText("\n\t\t\t\t${def.keyword} { $param${def.packageName}.${def.className}$ctor } $binds")
        }
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
}

fun String.dotPackage() = if (isNotBlank()) "$this." else ""

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}