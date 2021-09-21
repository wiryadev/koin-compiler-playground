import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSDeclaration
import generator.*
import generator.KoinCodeGenerator.Companion.LOGGER
import metadata.KoinMetaData
import java.io.OutputStream

fun OutputStream.generateFieldModule(module: KoinMetaData.Module) {
    module.definitions.filterIsInstance<KoinMetaData.Definition.ClassDeclarationDefinition>().forEach { def ->
        generateClassDeclarationDefinition(def)
    }
}

fun generateClassModule(classFile: OutputStream, module: KoinMetaData.Module) {
    classFile.appendText(MODULE_HEADER)
    classFile.appendText(module.definitions.generateImports())

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

fun KoinCodeGenerator.generateDefaultModuleForDefinitions(
    definitions: List<KoinMetaData.Definition>
) {
    definitions.forEachIndexed { index, def ->
        if (index == 0) {
            codeGenerator.getDefaultFile().apply {
                generateDefaultModuleHeader(definitions)
            }
        }
        logger.warn("generate $def")
        if (def is KoinMetaData.Definition.ClassDeclarationDefinition) {
            codeGenerator.getDefaultFile().generateClassDeclarationDefinition(def)
        }
        if (index == definitions.size - 1) {
            codeGenerator.getDefaultFile().apply {
                generateDefaultModuleFooter()
            }
        }
    }
}

fun OutputStream.generateDefaultModuleHeader(definitions: List<KoinMetaData.Definition> = emptyList()) {
    appendText(DEFAULT_MODULE_HEADER)
    appendText(definitions.generateImports())
    appendText(DEFAULT_MODULE_FUNCTION)
}

fun OutputStream.generateDefaultModuleFooter() {
    appendText(DEFAULT_MODULE_FOOTER)
}

private fun List<KoinMetaData.Definition>.generateImports() : String {
    return mapNotNull { definition -> definition.keyword.import?.let { "import $it" } }.joinToString(separator = "\n",postfix = "\n")
}