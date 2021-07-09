import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import org.koin.core.annotation.Module

class KoinMetaDataScanner(
    val logger: KSPLogger
) {

    lateinit var moduleMap : Map<String, KoinMetaData.Module>

    fun scanMetaData(resolver: Resolver, defaultModule: KoinMetaData.Module): Pair<Map<String, KoinMetaData.Module>, List<KoinMetaData.Definition>> {
        return scanClassModules(resolver, defaultModule)
    }

    private fun scanClassModules(
        resolver: Resolver,
        defaultModule: KoinMetaData.Module
    ): Pair<Map<String, KoinMetaData.Module>, List<KoinMetaData.Definition>> {

        logger.warn("scan modules ...")
        moduleMap = resolver.getSymbolsWithAnnotation(Module::class.qualifiedName!!)
            .filter { it is KSClassDeclaration && it.validate() }
            .map { indexClassModule(it) }
            .toMap()


        //TODO Scan annotations later (declared classes ...)
        val scannedDefinitions = emptyList<KoinMetaData.Definition>()
//        logger.warn("scan definitions ...")
//        val definitions = resolver.getSymbolsWithAnnotation(Single::class.qualifiedName!!)
//            .filter { it is KSClassDeclaration && it.validate() }
//            .mapNotNull { linkDefinition(it, defaultModule) }
//            .toList()

        return Pair(moduleMap, scannedDefinitions)
    }

    private fun indexClassModule(element: KSAnnotated): ModuleIndex {
        val declaration = (element as KSClassDeclaration)
        logger.warn("module(Class) -> $element", element)
        val modulePackage = declaration.containingFile?.packageName?.asString() ?: ""
        val name = "$element"
        val moduleMetadata = KoinMetaData.Module(
            packageName = modulePackage,
            name = name,
            type = KoinMetaData.ModuleType.CLASS
        )

        val annotatedFunctions = declaration.getAllFunctions()
            .filter { it.annotations.map { a -> a.shortName.asString() }.contains("Single") }
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
            val definition = KoinMetaData.Definition.FunctionDeclarationDefinition.Single(
                packageName = packageName,
                functionName = functionName,
                parameters = ksFunctionDeclaration.parameters.map { KoinMetaData.ConstructorParameter() }
                    ?: emptyList(),
                returnedType = type
            )
            definition
        }
    }

    private fun indexFieldModule(it: KSAnnotated): ModuleIndex {
        val declaration = (it as KSPropertyDeclaration)
        logger.warn("module(field) -> $it", it)
        val modulePackage = declaration.containingFile?.packageName?.asString() ?: ""
        val name = "$it"
        val moduleMetadata = KoinMetaData.Module(
            packageName = modulePackage,
            name = name
        )
        return ModuleIndex(modulePackage, moduleMetadata)
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
        val moduleKey = moduleMap.keys.firstOrNull { definition.packageName.contains(it) }
        val module = moduleMap[moduleKey] ?: defaultModule
        module.definitions.add(definition)
    }
}

typealias ModuleIndex = Pair<String, KoinMetaData.Module>