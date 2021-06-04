import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Single
import java.io.OutputStream

typealias ModuleIndex = Pair<String, KoinMetaData.Module>

class BuilderProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {

    val koinCodeGenerator = KoinCodeGenerator(codeGenerator,logger)
    val koinMetaDataScanner = KoinMetaDataScanner(logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
//        val ret = singleComponents.filter { !it.validate() }.toList()

        val defaultModule = KoinMetaData.Module(
            packageName = "",
            fieldName = "defaultModule"
        )
        val (moduleMap,definitions) = koinMetaDataScanner.scanMetaData(resolver,defaultModule)
        if (moduleMap.isNotEmpty()){
            koinCodeGenerator.generateModules(moduleMap, defaultModule)
        } else {
            koinCodeGenerator.generateDefinitions(definitions)
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