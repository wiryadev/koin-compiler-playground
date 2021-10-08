import org.koin.compiler.generator.*
import org.koin.compiler.metadata.KoinMetaData
import java.io.OutputStream

fun OutputStream.generateFieldModule(definitions: List<KoinMetaData.Definition>) {
    val classDefinitions = definitions.filterIsInstance<KoinMetaData.Definition.ClassDeclarationDefinition>()
    val standardDefinitions = classDefinitions.filter { it !is KoinMetaData.ScopeDefinition }
    standardDefinitions.forEach { def ->
        generateClassDeclarationDefinition(def)
    }

    val scopeDefinitions = classDefinitions.filter { it is KoinMetaData.ScopeDefinition }
    scopeDefinitions.filterIsInstance<KoinMetaData.ScopeDefinition>()
        .groupBy { it.scope }
        .forEach { (scope, definitions) ->
            KoinCodeGenerator.LOGGER.warn("generate scope $scope")
            appendText(generateScope(scope))
            definitions.forEach { generateClassDeclarationDefinition(it as KoinMetaData.Definition.ClassDeclarationDefinition) }

            // close scope
            appendText("\n\t\t\t\t}")
        }
}

fun generateClassModule(classFile: OutputStream, module: KoinMetaData.Module) {
    classFile.appendText(MODULE_HEADER)
    classFile.appendText(module.definitions.generateImports())

    val generatedField = "${module.name}Module"
    val classModule = "${module.packageName}.${module.name}"
    classFile.appendText("\nval $generatedField = module {")
    classFile.appendText("\n\t\t\t\tval moduleInstance = $classModule()")

    val standardDefinitions = module.definitions.filter { it !is KoinMetaData.ScopeDefinition }

    KoinCodeGenerator.LOGGER.warn("generate - definitions")

    standardDefinitions.forEach {
        when (it) {
            is KoinMetaData.Definition.FunctionDeclarationDefinition -> classFile.generateFunctionDeclarationDefinition(it)
            is KoinMetaData.Definition.ClassDeclarationDefinition -> classFile.generateClassDeclarationDefinition(it)
        }
    }

    KoinCodeGenerator.LOGGER.warn("generate - scopes")
    val scopeDefinitions = module.definitions.filter { it is KoinMetaData.ScopeDefinition }
    scopeDefinitions.filterIsInstance<KoinMetaData.ScopeDefinition>().groupBy { it.scope }
        .forEach { (scope, definitions) ->
            KoinCodeGenerator.LOGGER.warn("generate - scope $scope")
            classFile.appendText(generateScope(scope))
            definitions.forEach {
                when (it) {
                    is KoinMetaData.Definition.FunctionDeclarationDefinition -> classFile.generateFunctionDeclarationDefinition(it)
                    is KoinMetaData.Definition.ClassDeclarationDefinition -> classFile.generateClassDeclarationDefinition(it)
                }
            }
            // close scope
            classFile.appendText("\n\t\t\t\t}")
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
        codeGenerator.getDefaultFile().generateFieldModule(definitions)
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

private fun List<KoinMetaData.Definition>.generateImports(): String {
    return mapNotNull { definition -> definition.keyword.import?.let { "import $it" } }.joinToString(separator = "\n", postfix = "\n")
}