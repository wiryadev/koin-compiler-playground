import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*

class ModuleMetadataScanner(
    val logger: KSPLogger
) {

    fun createClassModule(element: KSAnnotated): ModuleIndex {
        val declaration = (element as KSClassDeclaration)
        logger.warn("module(Class) -> $element", element)
        val modulePackage = declaration.containingFile?.packageName?.asString() ?: ""

        val useComponentScan = (declaration.annotations.firstOrNull { it.shortName.asString() == "ComponentScan" } != null)
        logger.warn("module(Class) useComponentScan=$useComponentScan", element)

        val name = "$element"
        val moduleMetadata = KoinMetaData.Module(
            packageName = modulePackage,
            name = name,
            type = KoinMetaData.ModuleType.CLASS,
            componentScan = useComponentScan
        )

        val annotatedFunctions = declaration.getAllFunctions()
            .filter {
                it.annotations.map { a -> a.shortName.asString() }.any { a -> KoinDefinitionAnnotation.isValidAnnotation(a) }
            }
            .toList()

        logger.warn("module(Class) -> $element | found functions: ${annotatedFunctions.size}", element)
        val definitions = annotatedFunctions.mapNotNull { addDefinition(it) }
        moduleMetadata.definitions += definitions

        return ModuleIndex(modulePackage, moduleMetadata)
    }

    private fun addDefinition(element: KSAnnotated): KoinMetaData.Definition? {
        logger.warn("definition(function) -> $element", element)

        val ksFunctionDeclaration = (element as KSFunctionDeclaration)
        val packageName = ksFunctionDeclaration.containingFile!!.packageName.asString()
        val type = ksFunctionDeclaration.returnType?.resolve()?.declaration?.simpleName?.toString()

        return type?.let {
            val functionName = ksFunctionDeclaration.simpleName.asString()

            element.getDefinitionAnnotation()?.let { (name,annotation) ->
                logger.warn("definition(function) -> kind $name", annotation)
                logger.warn("definition(function) -> kind ${annotation.arguments}", annotation)

                when (KoinDefinitionAnnotation.valueOf(name)) {
                    KoinDefinitionAnnotation.Single -> {
                        val createdAtStart: Boolean = annotation.arguments.firstOrNull { it.name?.asString() == "createdAtStart" }?.value as Boolean? ?: false
                        val binds = annotation.arguments.firstOrNull { it.name?.asString() == "binds" }?.value as? List<KSType>?
                        logger.warn("definition(function) -> createdAtStart=$createdAtStart", annotation)
                        logger.warn("definition(function) -> binds=$binds", annotation)
                        KoinMetaData.Definition.FunctionDeclarationDefinition.Single(
                            packageName = packageName,
                            functionName = functionName,
                            functionParameters = ksFunctionDeclaration.parameters.map { KoinMetaData.ConstructorParameter() },
                            returnedType = type,
                            createdAtStart = createdAtStart,
                            bindings = binds?.map { it.declaration } ?: emptyList()
                        )
                    }
                    KoinDefinitionAnnotation.Factory -> {
                        val binds = annotation.arguments.firstOrNull { it.name?.asString() == "binds" }?.value as? List<KSType>?
                        KoinMetaData.Definition.FunctionDeclarationDefinition.Factory(
                            packageName = packageName,
                            functionName = functionName,
                            functionParameters = ksFunctionDeclaration.parameters.map { KoinMetaData.ConstructorParameter() },
                            returnedType = type,
                            bindings = binds?.map { it.declaration } ?: emptyList()
                        )
                    }
                    else -> null
                }
            }
        }
    }
}

private fun KSAnnotated.getDefinitionAnnotation(): Pair<String,KSAnnotation>? {
    return try {
        val a = annotations.firstOrNull { a -> KoinDefinitionAnnotation.isValidAnnotation(a.shortName.asString()) }
        a?.let { Pair(a.shortName.asString(),a) }
    } catch (e: Exception) {
        null
    }
}