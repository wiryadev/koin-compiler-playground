import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
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
    
        fun KoinApplication.allModules(modules : List<Module> = emptyList(), useDefaultModule : Boolean = true) {
    """.trimIndent()

    val allModulesFooter = """
            if (useDefaultModule){
                modules(defaultModule+modules)
            } else {
                modules(modules)
            }
        }
    """.trimIndent()

    val defaultModuleGen = """
        val defaultModule = Module()
    """.trimIndent()


    fun generate(moduleMap: Map<String, KoinMetaData.Module>, defaultModule: KoinMetaData.Module) {
        logger.warn("generate ...")
        moduleMap.values.forEachIndexed { index, module ->
            if (index == 0){
                val file = getDefaultFile()
                file.appendText(allModulesHeader)
            }
            generateModule(module)
            if (index == moduleMap.values.size -1){
                generateDefaultModule(defaultModule)
                val file = getDefaultFile()
                file.appendText("\n"+allModulesFooter)
            }
        }
    }

    private fun generateDefaultModule(defaultModule: KoinMetaData.Module) {
        val file = getDefaultFile()
        file.appendText("\n\t\t"+defaultModuleGen)
        generateModule(defaultModule)
    }

    private fun generateModule(module: KoinMetaData.Module) {
        logger.warn("generate $module")
        val file = getDefaultFile()
        file.appendText("\n// module - ${module.packageName} : $module")
        val packageName = if (module.packageName.isNotBlank()) "${module.packageName}." else ""
        file.appendText("\n\t $packageName${module.fieldName}.apply {")
        module.definitions.forEach { def ->
            logger.warn("generate $def")
            generateDefinition(def)
        }
        file.appendText("\n\t}")
    }

    private fun generateDefinition(def: KoinMetaData.Definition) {
        val file = getDefaultFile()
        file.appendText("\n// def - ${def.packageName} : ${def.className}")
    }

    private fun getDefaultFile() = codeGenerator.createNewFile(
        Dependencies.ALL_FILES,
        "org.koin.ksp.generated",
        "Default"
    )
}

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}