import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

class KoinMetaDataScanner(
    val logger: KSPLogger
) {

    lateinit var moduleMap : Map<String, KoinMetaData.Module>

    fun scanAllMetaData(resolver: Resolver, defaultModule: KoinMetaData.Module): Pair<Map<String, KoinMetaData.Module>, List<KoinMetaData.Definition>> {
        return Pair(
            scanClassModules(resolver, defaultModule).toSortedMap(),
            scanComponents(resolver, defaultModule)
        )
    }

    private fun scanClassModules(
        resolver: Resolver,
        defaultModule: KoinMetaData.Module
    ): Map<String, KoinMetaData.Module> {

        logger.warn("scan modules ...")
        // class modules
        moduleMap = resolver.getSymbolsWithAnnotation(Module::class.qualifiedName!!)
            .filter { it is KSClassDeclaration && it.validate() }
            .map { indexClassModule(it) }
            .toMap()

        return moduleMap
    }

    private fun indexClassModule(element: KSAnnotated): ModuleIndex {
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
            .filter { it.annotations.map { a -> a.shortName.asString() }.any { a -> a in KoinDefinitionAnnotation.allValues } }
            .toList()

        logger.warn("module(Class) -> $element | found functions: ${annotatedFunctions.size}", element)
        val definitions = annotatedFunctions.mapNotNull { linkFunctionDefinition(it) }
        moduleMetadata.definitions += definitions

        return ModuleIndex(modulePackage, moduleMetadata)
    }

    private fun linkFunctionDefinition(element: KSAnnotated): KoinMetaData.Definition? {
        logger.warn("single(function) -> $element", element)
        val ksFunctionDeclaration = (element as KSFunctionDeclaration)
        val packageName = ksFunctionDeclaration.containingFile!!.packageName.asString()
        val type = ksFunctionDeclaration.returnType?.resolve()?.declaration?.simpleName?.toString()
        return type?.let {
            val functionName = ksFunctionDeclaration.simpleName.asString()
            KoinMetaData.Definition.FunctionDeclarationDefinition.Single(
                packageName = packageName,
                functionName = functionName,
                functionParameters = ksFunctionDeclaration.parameters.map { KoinMetaData.ConstructorParameter() }
                    ?: emptyList(),
                returnedType = type
            )
        }
    }

    private fun scanComponents(
        resolver: Resolver,
        defaultModule: KoinMetaData.Module
    ): List<KoinMetaData.Definition> {
        // component scan
        logger.warn("scan definitions ...")
        return resolver.getSymbolsWithAnnotation(Single::class.qualifiedName!!)
            .filter { it is KSClassDeclaration && it.validate() }
            .mapNotNull {
                linkDefinition(it, defaultModule)
            }
            .toList()
    }

    private fun linkDefinition(it: KSAnnotated, defaultModule: KoinMetaData.Module): KoinMetaData.Definition {
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
        addToModule(definition, defaultModule)
        return definition
    }

    private fun addToModule(definition: KoinMetaData.Definition, defaultModule: KoinMetaData.Module) {
        val definitionPackage = definition.packageName
        val foundModule = moduleMap.values.firstOrNull { definitionPackage.contains(it.packageName) && it.componentScan}
        val module = foundModule ?: defaultModule
        logger.warn("single(class) -> $definition -> module $module")
        module.definitions.add(definition)
    }
}

typealias ModuleIndex = Pair<String, KoinMetaData.Module>