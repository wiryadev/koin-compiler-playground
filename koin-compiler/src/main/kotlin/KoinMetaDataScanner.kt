import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Single

class KoinMetaDataScanner(
    val logger: KSPLogger
) {

    lateinit var moduleMap: Map<String, KoinMetaData.Module>

    fun scanMetaData(resolver: Resolver, defaultModule: KoinMetaData.Module): Map<String, KoinMetaData.Module> {
        logger.warn("scan modules ...")
        moduleMap = resolver.getSymbolsWithAnnotation(ComponentScan::class.qualifiedName!!)
            .filter { it is KSPropertyDeclaration && it.validate() }
            .map { indexModule(it) }.
            toMap()

        logger.warn("scan definitions ...")
        resolver.getSymbolsWithAnnotation(Single::class.qualifiedName!!)
            .filter { it is KSClassDeclaration && it.validate() }
            .map { linkDefinition(it, defaultModule) }
            .toList()
        return moduleMap
    }

    private fun indexModule(it: KSAnnotated): ModuleIndex {
        val declaration = (it as KSPropertyDeclaration)
        logger.warn("module -> $it", it)
        val modulePackage = declaration.containingFile?.packageName?.asString() ?: ""
        val name = "$it"
        val moduleMetadata = KoinMetaData.Module(
            packageName = modulePackage,
            fieldName = name
        )
        return ModuleIndex(modulePackage, moduleMetadata)
    }

    private fun linkDefinition(it: KSAnnotated, defaultModule: KoinMetaData.Module) {
        logger.warn("single -> $it", it)
        val ksClassDeclaration = (it as KSClassDeclaration)
        val packageName = ksClassDeclaration.containingFile!!.packageName.asString()
        val className = ksClassDeclaration.simpleName.asString()
        val definition = KoinMetaData.Definition.Single(
            packageName = packageName,
            className = className,
            constructorParameters = ksClassDeclaration.primaryConstructor?.parameters?.map { KoinMetaData.ConstructorParameter() }
                ?: emptyList(),
            bindings = ksClassDeclaration.superTypes.map { it.resolve().declaration }.toList()
        )
        addToModule(definition, defaultModule)
    }

    private fun addToModule(definition: KoinMetaData.Definition, defaultModule: KoinMetaData.Module) {
        val moduleKey = moduleMap.keys.firstOrNull { definition.packageName.contains(it) }
        val module = moduleMap[moduleKey] ?: defaultModule
        module.definitions.add(definition)
    }
}