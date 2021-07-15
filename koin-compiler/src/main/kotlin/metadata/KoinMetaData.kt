package metadata

import com.google.devtools.ksp.symbol.KSDeclaration

sealed class KoinMetaData {

    data class Module(
        val packageName: String,
        val name: String,
        val definitions: MutableList<Definition> = mutableListOf(),
        val type: ModuleType = ModuleType.FIELD,
        val componentScan: ComponentScan? = null
    ) : KoinMetaData(){
        data class ComponentScan(val packageName : String = "")

        fun acceptDefinition(defPackageName : String) : Boolean {
            return when {
                componentScan == null -> false
                componentScan.packageName.isNotEmpty() -> defPackageName.contains(componentScan.packageName, ignoreCase = true)
                componentScan.packageName.isEmpty() -> defPackageName.contains(packageName,ignoreCase = true)
                else -> false
            }
        }
    }

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
            bindings: List<KSDeclaration>
        ) : Definition(packageName, keyword, bindings) {

            class Single(
                packageName: String,
                functionName: String,
                functionParameters: List<ConstructorParameter> = emptyList(),
                val createdAtStart : Boolean = false,
                bindings: List<KSDeclaration>
            ) : FunctionDeclarationDefinition(packageName, "single", functionName, functionParameters, bindings)

            class Factory(
                packageName: String,
                functionName: String,
                functionParameters: List<ConstructorParameter> = emptyList(),
                bindings: List<KSDeclaration>
            ) : FunctionDeclarationDefinition(packageName, "factory", functionName, functionParameters, bindings)
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