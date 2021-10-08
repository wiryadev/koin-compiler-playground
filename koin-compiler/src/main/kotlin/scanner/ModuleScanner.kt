package scanner

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import metadata.*

class ModuleScanner(
    val logger: KSPLogger
) {

    fun createClassModule(element: KSAnnotated): ModuleIndex {
        val declaration = (element as KSClassDeclaration)
        logger.warn("module(Class) -> $element", element)
        val modulePackage = declaration.containingFile?.packageName?.asString() ?: ""

        val componentScan =
            getComponentScan(declaration)
        logger.warn("module(Class) componentScan=$componentScan", element)

        val name = "$element"
        val moduleMetadata = KoinMetaData.Module(
            packageName = modulePackage,
            name = name,
            type = KoinMetaData.ModuleType.CLASS,
            componentScan = componentScan
        )

        val annotatedFunctions = declaration.getAllFunctions()
            .filter {
                it.annotations.map { a -> a.shortName.asString() }.any { a -> isValidAnnotation(a) }
            }
            .toList()

        logger.warn("module(Class) -> $element | found class functions: ${annotatedFunctions.size}", element)
        val definitions = annotatedFunctions.mapNotNull { addDefinition(it) }
        moduleMetadata.definitions += definitions

        return ModuleIndex(componentScan?.packageName ?: modulePackage, moduleMetadata)
    }

    private fun getComponentScan(declaration: KSClassDeclaration): KoinMetaData.Module.ComponentScan? {
        val componentScan = declaration.annotations.firstOrNull { it.shortName.asString() == "ComponentScan" }
        return componentScan?.let { a ->
            val value : String = a.arguments.firstOrNull { arg -> arg.name?.asString() == "value" }?.value as? String? ?: ""
            KoinMetaData.Module.ComponentScan(value)
        }
    }

    private fun addDefinition(element: KSAnnotated): KoinMetaData.Definition? {
        logger.warn("definition(function) -> $element", element)

        val ksFunctionDeclaration = (element as KSFunctionDeclaration)
        val packageName = ksFunctionDeclaration.containingFile!!.packageName.asString()
        val returnedType = ksFunctionDeclaration.returnType?.resolve()?.declaration?.simpleName?.toString()
        val qualifier = ksFunctionDeclaration.getStringQualifier()

        return returnedType?.let {
            val functionName = ksFunctionDeclaration.simpleName.asString()

            val annotations = element.getKoinAnnotations()
            val scopeAnnotation = annotations.getScopeAnnotation()

            return if (scopeAnnotation != null){
                declareDefinition(scopeAnnotation.first, scopeAnnotation.second, packageName, qualifier, functionName, ksFunctionDeclaration)
            } else {
                annotations.firstNotNullOf { (annotationName, annotation) ->
                    declareDefinition(annotationName, annotation, packageName, qualifier, functionName, ksFunctionDeclaration)
                }
            }
        }
    }

    private fun declareDefinition(
        annotationName: String,
        annotation: KSAnnotation,
        packageName: String,
        qualifier: String?,
        functionName: String,
        ksFunctionDeclaration: KSFunctionDeclaration
    ): KoinMetaData.Definition.FunctionDeclarationDefinition? {
        logger.warn("definition(function) -> kind $annotationName", annotation)
        logger.warn("definition(function) -> kind ${annotation.arguments}", annotation)

        val binds = annotation.arguments.firstOrNull { it.name?.asString() == "binds" }?.value as? List<KSType>?
        logger.warn("definition(function) -> binds=$binds", annotation)

        val functionParameters = ksFunctionDeclaration.parameters.getConstructorParameters()
        logger.warn("definition(function) ctor -> $functionParameters", annotation)
        return when (annotationName) {
            SINGLE.annotationName -> {
                val createdAtStart: Boolean =
                    annotation.arguments.firstOrNull { it.name?.asString() == "createdAtStart" }?.value as Boolean?
                        ?: false
                logger.warn("definition(function) -> createdAtStart=$createdAtStart", annotation)
                KoinMetaData.Definition.FunctionDeclarationDefinition.Single(
                    packageName = packageName,
                    qualifier = qualifier,
                    functionName = functionName,
                    functionParameters = functionParameters,
                    createdAtStart = createdAtStart,
                    bindings = binds?.map { it.declaration } ?: emptyList()
                )
            }
            FACTORY.annotationName -> {
                KoinMetaData.Definition.FunctionDeclarationDefinition.Factory(
                    packageName = packageName,
                    qualifier = qualifier,
                    functionName = functionName,
                    functionParameters = functionParameters,
                    bindings = binds?.map { it.declaration } ?: emptyList()
                )
            }
            KOIN_VIEWMODEL.annotationName -> {
                KoinMetaData.Definition.FunctionDeclarationDefinition.ViewModel(
                    packageName = packageName,
                    qualifier = qualifier,
                    functionName = functionName,
                    functionParameters = functionParameters,
                    bindings = binds?.map { it.declaration } ?: emptyList()
                )
            }
            SCOPE.annotationName -> {
                //TODO Any other annotation?
                val scopeData : KoinMetaData.Scope = annotation.arguments.getScope()
                logger.warn("definition(function) -> scope $scopeData", annotation)
                KoinMetaData.Definition.FunctionDeclarationDefinition.Scope(
                    packageName = packageName,
                    qualifier = qualifier,
                    functionName = functionName,
                    functionParameters = functionParameters,
                    bindings = binds?.map { it.declaration } ?: emptyList(),
                    scope = scopeData
                )
            }
            else -> null
        }
    }
}