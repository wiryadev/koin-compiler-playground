import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class ComponentMetadataScanner(
    val logger: KSPLogger,
) {

    fun createDefinition(it: KSAnnotated): KoinMetaData.Definition {
        logger.warn("single(class) -> $it", it)
        val ksClassDeclaration = (it as KSClassDeclaration)
        val packageName = ksClassDeclaration.containingFile!!.packageName.asString()
        val className = ksClassDeclaration.simpleName.asString()
        val definition = KoinMetaData.Definition.ClassDeclarationDefinition.Single(
            packageName = packageName,
            className = className,
            constructorParameters = ksClassDeclaration.primaryConstructor?.parameters?.map { KoinMetaData.ConstructorParameter() }
                ?: emptyList(),
            bindings = ksClassDeclaration.superTypes.map { it.resolve().declaration }.toList()
        )
        return definition
    }
}