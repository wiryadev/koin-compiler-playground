package scanner

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
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
        return element.getDefinitionAnnotation()?.let { (name, annotation) ->
            val declaredBindingsTypes = annotation.arguments.firstOrNull { it.name?.asString() == "binds" }?.value as? List<KSType>?
            val declaredBindings = declaredBindingsTypes?.map { it.declaration }
            val defaultBindings = ksClassDeclaration.superTypes.map { it.resolve().declaration }.toList()
            when (name.toLowerCase()) {
                SINGLE.name -> {
                    val createdAtStart: Boolean = annotation.arguments.firstOrNull { it.name?.asString() == "createdAtStart" }?.value as Boolean? ?: false
                    val allBindings = if (declaredBindings?.isNotEmpty() == true) declaredBindings else defaultBindings
                    logger.warn("definition(class) bindings -> $allBindings", element)
                    KoinMetaData.Definition.ClassDeclarationDefinition.Single(
                        packageName = packageName,
                        qualifier = qualifier,
                        className = className,
                        constructorParameters = ksClassDeclaration.primaryConstructor?.parameters?.map { KoinMetaData.ConstructorParameter() }
                            ?: emptyList(),
                        bindings = allBindings,
                        createdAtStart = createdAtStart
                    )
                }
                FACTORY.name -> {
                    KoinMetaData.Definition.ClassDeclarationDefinition.Factory(
                        packageName = packageName,
                        qualifier = qualifier,
                        className = className,
                        constructorParameters = ksClassDeclaration.primaryConstructor?.parameters?.map { KoinMetaData.ConstructorParameter() }
                            ?: emptyList(),
                        bindings = declaredBindings ?: defaultBindings
                    )
                }
                VIEWMODEL.name -> {
                    KoinMetaData.Definition.ClassDeclarationDefinition.ViewModel(
                        packageName = packageName,
                        qualifier = qualifier,
                        className = className,
                        constructorParameters = ksClassDeclaration.primaryConstructor?.parameters?.map { KoinMetaData.ConstructorParameter() }
                            ?: emptyList(),
                        bindings = declaredBindings ?: defaultBindings
                    )
                }
                else -> error("Unknown annotation type: $name")
            }
        } ?: error("Can't create definition found for $element")
    }
}