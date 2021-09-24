import com.google.devtools.ksp.symbol.KSDeclaration
import generator.KoinCodeGenerator.Companion.LOGGER
import metadata.KoinMetaData
import java.io.OutputStream


fun OutputStream.generateFunctionDeclarationDefinition(
    def: KoinMetaData.Definition.FunctionDeclarationDefinition
) {
    LOGGER.warn("generate $def")
    val ctor = generateClassConstructor(def.parameters)
    val binds = generateBindings(def.bindings)
    val qualifier = def.qualifier.generateQualifier()
    val createAtStart = if (def is KoinMetaData.Definition.FunctionDeclarationDefinition.Single){
        if (def.createdAtStart) CREATED_AT_START else ""
    } else ""
    appendText("\n\t\t\t\t${def.keyword.keyword}($qualifier$createAtStart) { moduleInstance.${def.functionName}$ctor } $binds")
}


fun OutputStream.generateClassDeclarationDefinition(def: KoinMetaData.Definition.ClassDeclarationDefinition) {
    LOGGER.warn("generate $def")
    val param =
        if (def.constructorParameters.filter { it is KoinMetaData.ConstructorParameter.ParameterInject }
                .isEmpty()) "" else " params ->"
    val ctor = generateClassConstructor(def.constructorParameters)
    val binds = generateBindings(def.bindings)
    val qualifier = def.qualifier.generateQualifier()
    val createAtStart = if (def is KoinMetaData.Definition.ClassDeclarationDefinition.Single){
        if (def.createdAtStart) CREATED_AT_START else ""
    } else ""
    appendText("\n\t\t\t\t${def.keyword.keyword}($qualifier$createAtStart) { $param${def.packageName}.${def.className}$ctor } $binds")
}

const val CREATED_AT_START=",createdAtStart=true"

fun String?.generateQualifier():String = when {
    this == "\"null\"" -> "qualifier=null"
    this == "null" -> "qualifier=null"
    !this.isNullOrBlank() -> "qualifier=StringQualifier(\"$this\")"
    else -> "qualifier=null"
}

fun generateBindings(bindings: List<KSDeclaration>): String {
    return when {
        bindings.isEmpty() -> ""
        bindings.size == 1 -> "bind(${generateBinding(bindings.first())})"
        else -> bindings.joinToString(prefix = "binds(", separator = ",", postfix = ")") { generateBinding(it) }
    }
}

fun generateBinding(declaration: KSDeclaration): String {
    val packageName = declaration.containingFile!!.packageName.asString()
    val className = declaration.simpleName.asString()
    return "$packageName.$className::class"
}

fun generateClassConstructor(constructorParameters: List<KoinMetaData.ConstructorParameter>): String {
    return constructorParameters.joinToString(prefix = "(", separator = ",", postfix = ")") {
        when (it) {
            is KoinMetaData.ConstructorParameter.Dependency -> "get()" // value -> qualifier = StringQualifier("\"${it.value}\"")
            is KoinMetaData.ConstructorParameter.ParameterInject -> "params.get()"
            is KoinMetaData.ConstructorParameter.Property -> "getProperty(\"${it.value}\")"
        }
    }
}