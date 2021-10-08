package org.koin.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import generateClassModule
import generateDefaultModuleFooter
import generateDefaultModuleForDefinitions
import generateDefaultModuleHeader
import generateFieldModule
import org.koin.compiler.metadata.KoinMetaData

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
        logger.warn("generate ${moduleMap.size} modules ...")
        moduleMap.values.forEachIndexed { index, module ->
            if (index == 0) {
                if (defaultModule.definitions.isNotEmpty()){
                    codeGenerator.getDefaultFile().generateDefaultModuleHeader()
                }
            }
            generateModule(module)
            if (index == moduleMap.values.size - 1) {
                generateModule(defaultModule)
                if (defaultModule.definitions.isNotEmpty()) {
                    codeGenerator.getDefaultFile().generateDefaultModuleFooter()
                }
            }
        }
    }

    private fun generateModule(module: KoinMetaData.Module) {
        logger.warn("generate $module - ${module.type}")
        codeGenerator.getDefaultFile().let { defaultFile ->
            if (module.definitions.isNotEmpty()) {
                when (module.type) {
                    KoinMetaData.ModuleType.FIELD -> defaultFile.generateFieldModule(module.definitions)
                    KoinMetaData.ModuleType.CLASS -> {
                        val moduleFile = codeGenerator.getFile(fileName = "${module.name}Gen")
                        generateClassModule(moduleFile, module)
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