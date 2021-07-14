package metadata

import com.google.devtools.ksp.symbol.KSDeclaration

enum class KoinDefinitionAnnotation {
    Single, Factory;

    companion object {
        val allValues : List<String> = values().map { it.toString() }
        fun isValidAnnotation(s : String) : Boolean = s in allValues
    }
}

sealed class KoinMetaData {

    data class Module(
        val packageName: String,
        val name: String,
        val definitions: MutableList<Definition> = mutableListOf(),
        val type: ModuleType = ModuleType.FIELD,
        val componentScan: Boolean = false
    ) : KoinMetaData()

    sealed class Definition(
        val packageName: String,
        val keyword: String,
        val bindings: List<KSDeclaration>
    ) : KoinMetaData() {

        sealed class FunctionDeclarationDefinition(
            packageName: String,
            keyword: String,
            val functionName: String,
            val parameters: List<ConstructorParameter> = emptyList(),
            val returnedType: String,
            bindings: List<KSDeclaration>
        ) : Definition(packageName, keyword, bindings) {

            class Single(
                packageName: String,
                functionName: String,
                functionParameters: List<ConstructorParameter> = emptyList(),
                returnedType: String,
                val createdAtStart : Boolean = false,
                bindings: List<KSDeclaration>
            ) : FunctionDeclarationDefinition(packageName, "single", functionName, functionParameters, returnedType,bindings)

            class Factory(
                packageName: String,
                functionName: String,
                functionParameters: List<ConstructorParameter> = emptyList(),
                returnedType: String,
                bindings: List<KSDeclaration>
            ) : FunctionDeclarationDefinition(packageName, "factory", functionName, functionParameters, returnedType,bindings)
        }

        sealed class ClassDeclarationDefinition(
            packageName: String,
            keyword: String,
            val className: String,
            val constructorParameters: List<ConstructorParameter> = emptyList(),
            bindings: List<KSDeclaration>,
        ) : Definition(packageName, keyword,bindings) {

            class Single(
                packageName: String,
                className: String,
                constructorParameters: List<ConstructorParameter> = emptyList(),
                val createdAtStart : Boolean = false,
                bindings: List<KSDeclaration>
            ) : ClassDeclarationDefinition(packageName, "single", className, constructorParameters, bindings)

            class Factory(
                packageName: String,
                className: String,
                constructorParameters: List<ConstructorParameter> = emptyList(),
                val createdAtStart : Boolean = false,
                bindings: List<KSDeclaration>
            ) : ClassDeclarationDefinition(packageName, "factory", className, constructorParameters, bindings)
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