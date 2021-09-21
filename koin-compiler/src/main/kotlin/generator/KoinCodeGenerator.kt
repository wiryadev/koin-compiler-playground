package generator

import appendText
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import generateClassDeclarationDefinition
import generateClassModule
import generateDefaultModuleForDefinitions
import generateFieldModule
import metadata.KoinMetaData

class KoinCodeGenerator(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) {

    init {
        LOGGER = logger
    }

    fun generateModules(
        moduleMap: Map<String, KoinMetaData.Module>,
        defaultModule: KoinMetaData.Module
    ) {
        logger.warn("generate modules ...")
        moduleMap.values.forEachIndexed { index, module ->
            if (index == 0) {
                val file = codeGenerator.getDefaultFile()
                file.appendText(DEFAULT_MODULE_HEADER)
            }
            generateModule(module)
            if (index == moduleMap.values.size - 1) {
                generateModule(defaultModule)
                val file = codeGenerator.getDefaultFile()
                file.appendText("\n" + DEFAULT_MODULE_FOOTER)
            }
        }
    }

    private fun generateModule(module: KoinMetaData.Module) {
        logger.warn("generate $module - ${module.type}")
        codeGenerator.getDefaultFile().let { defaultFile ->
            if (module.definitions.isNotEmpty()) {
                when (module.type) {
                    KoinMetaData.ModuleType.FIELD -> defaultFile.generateFieldModule(module)
                    KoinMetaData.ModuleType.CLASS -> {
                        val moduleFile = codeGenerator.getFile(fileName = "${module.name}Gen")
                        generateClassModule(moduleFile, module, logger)
                    }
                }
            } else {
                logger.warn("no definition for $module")
            }
        }
    }

    fun generateDefaultModule(
        definitions: List<KoinMetaData.Definition>
    ) {
        generateDefaultModuleForDefinitions(definitions)
    }

    companion object {
        lateinit var LOGGER: KSPLogger
            private set
    }
}

fun CodeGenerator.getDefaultFile() = createNewFile(
    Dependencies.ALL_FILES,
    "org.koin.ksp.generated",
    "Default"
)

fun CodeGenerator.getFile(packageName: String = "org.koin.ksp.generated", fileName: String) = createNewFile(
    Dependencies.ALL_FILES,
    packageName,
    fileName
)