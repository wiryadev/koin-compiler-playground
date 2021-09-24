package scanner

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import metadata.*

class ComponentScanner(
    val logger: KSPLogger,
) {

    fun createDefinition(element: KSAnnotated): KoinMetaData.Definition {
        logger.warn("definition(class) -> $element", element)
        val ksClassDeclaration = (element as KSClassDeclaration)
        val packageName = ksClassDeclaration.containingFile!!.packageName.asString()
        val className = ksClassDeclaration.simpleName.asString()
        val qualifier = ksClassDeclaration.getStringQualifier()
        logger.warn("definition(class) qualifier -> $qualifier", element)
        return element.getDefinitionAnnotation()?.let { (annotationName, annotation) ->
            logger.warn("definition(class) bindings ...", element)
            val declaredBindingsTypes = annotation.arguments.firstOrNull { it.name?.asString() == "binds" }?.value as? List<KSType>?
            val declaredBindings = declaredBindingsTypes?.map { it.declaration }
            val defaultBindings = ksClassDeclaration.superTypes.map { it.resolve().declaration }.toList()
            val allBindings = if (declaredBindings?.isNotEmpty() == true) declaredBindings else defaultBindings
            logger.warn("definition(class) bindings -> $allBindings", element)

            val ctorParams = ksClassDeclaration.primaryConstructor?.parameters?.getConstructorParameters()
            logger.warn("definition(class) ctor -> $ctorParams", element)
            when (annotationName) {
                SINGLE.annotationName -> {
                    val createdAtStart: Boolean = annotation.arguments.firstOrNull { it.name?.asString() == "createdAtStart" }?.value as Boolean? ?: false
                    KoinMetaData.Definition.ClassDeclarationDefinition.Single(
                        packageName = packageName,
                        qualifier = qualifier,
                        className = className,
                        constructorParameters = ctorParams
                            ?: emptyList(),
                        bindings = allBindings,
                        createdAtStart = createdAtStart
                    )
                }
                FACTORY.annotationName -> {
                    KoinMetaData.Definition.ClassDeclarationDefinition.Factory(
                        packageName = packageName,
                        qualifier = qualifier,
                        className = className,
                        constructorParameters = ctorParams
                            ?: emptyList(),
                        bindings = declaredBindings ?: defaultBindings
                    )
                }
                KOIN_VIEWMODEL.annotationName -> {
                    KoinMetaData.Definition.ClassDeclarationDefinition.ViewModel(
                        packageName = packageName,
                        qualifier = qualifier,
                        className = className,
                        constructorParameters = ctorParams
                            ?: emptyList(),
                        bindings = declaredBindings ?: defaultBindings
                    )
                }
                else -> error("Unknown annotation type: $annotationName")
            }
        } ?: error("No valid definition annotation found for $element")
    }
}