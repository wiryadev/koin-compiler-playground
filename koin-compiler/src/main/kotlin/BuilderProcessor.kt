import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import generator.KoinCodeGenerator
import metadata.KoinMetaData
import scanner.KoinMetaDataScanner

class BuilderProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {

    val koinCodeGenerator = KoinCodeGenerator(codeGenerator, logger)
    val koinMetaDataScanner = KoinMetaDataScanner(logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val defaultModule = KoinMetaData.Module(
            packageName = "",
            name = "defaultModule"
        )
        logger.warn("Scan metadata ...")
        val (moduleMap, definitions) = koinMetaDataScanner.scanAllMetaData(resolver, defaultModule)
        logger.warn("Code generation ...")
        if (moduleMap.isNotEmpty()) {
            koinCodeGenerator.generateModules(moduleMap, defaultModule)
        } else {
            koinCodeGenerator.generateDefaultModule(definitions)
        }
        return emptyList()
    }
}

class BuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return BuilderProcessor(environment.codeGenerator, environment.logger)
    }
}