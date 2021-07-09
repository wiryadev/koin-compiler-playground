import com.google.devtools.ksp.symbol.KSDeclaration

sealed class KoinMetaData {

    data class Module(
        val packageName: String,
        val name: String,
        val definitions: MutableList<Definition> = mutableListOf(),
        val type: ModuleType = ModuleType.FIELD
    ) : KoinMetaData()

    sealed class Definition(
        val packageName: String,
        val keyword: String
    ) : KoinMetaData() {

        sealed class FunctionDeclarationDefinition(
            packageName: String,
            keyword: String,
            val functionName: String,
            val parameters: List<ConstructorParameter> = emptyList(),
            val returnedType: String,
        ) : Definition(packageName, keyword) {

            class Single(
                packageName: String,
                functionName: String,
                parameters: List<ConstructorParameter> = emptyList(),
                returnedType: String,
            ) : FunctionDeclarationDefinition(packageName, "single", functionName, parameters, returnedType)
        }

        sealed class ClassDeclarationDefinition(
            packageName: String,
            keyword: String,
            val className: String,
            val constructorParameters: List<ConstructorParameter> = emptyList(),
            val bindings: List<KSDeclaration>,
        ) : Definition(packageName, keyword) {

            class Single(
                packageName: String,
                className: String,
                constructorParameters: List<ConstructorParameter> = emptyList(),
                bindings: List<KSDeclaration>
            ) : ClassDeclarationDefinition(packageName, "single", className, constructorParameters, bindings)
        }

    }

    enum class ModuleType {
        FIELD, CLASS
    }

    data class ConstructorParameter(
        val qualifier: String? = null,
        val type: ConstructorParameterType = ConstructorParameterType.DEPENDENCY
    )

    enum class ConstructorParameterType {
        DEPENDENCY, PARAMETER_INJECT
    }
}