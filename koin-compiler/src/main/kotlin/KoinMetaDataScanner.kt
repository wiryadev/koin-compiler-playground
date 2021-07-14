import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

class KoinMetaDataScanner(
    val logger: KSPLogger
) {

    lateinit var moduleMap: Map<String, KoinMetaData.Module>
    private val moduleMetadataScanner = ModuleMetadataScanner(logger)
    private val componentMetadataScanner = ComponentMetadataScanner(logger)

    fun scanAllMetaData(
        resolver: Resolver,
        defaultModule: KoinMetaData.Module
    ): Pair<Map<String, KoinMetaData.Module>, List<KoinMetaData.Definition>> {
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
            .map { moduleMetadataScanner.createClassModule(it) }
            .toMap()

        return moduleMap
    }

    private fun scanComponents(
        resolver: Resolver,
        defaultModule: KoinMetaData.Module
    ): List<KoinMetaData.Definition> {
        // component scan
        logger.warn("scan definitions ...")
        val definitions = resolver.getSymbolsWithAnnotation(Single::class.qualifiedName!!)
            .filter { it is KSClassDeclaration && it.validate() }
            .mapNotNull { componentMetadataScanner.createDefinition(it) }
            .toList()
        definitions.forEach { addToModule(it, defaultModule) }
        return definitions
    }

    private fun addToModule(definition: KoinMetaData.Definition, defaultModule: KoinMetaData.Module) {
        val definitionPackage = definition.packageName
        val foundModule = moduleMap.values.firstOrNull { definitionPackage.contains(it.packageName) && it.componentScan }
        val module = foundModule ?: defaultModule
        logger.warn("single(class) -> $definition -> module $module")
        module.definitions.add(definition)
    }
}

typealias ModuleIndex = Pair<String, KoinMetaData.Module>