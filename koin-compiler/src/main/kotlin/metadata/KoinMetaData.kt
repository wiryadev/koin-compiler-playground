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
        val qualifier : String? = null,
        val keyword: String,
        val bindings: List<KSDeclaration>,
        val isAndroidDefinition : Boolean = false
    ) : KoinMetaData() {

        sealed class FunctionDeclarationDefinition(
            packageName: String,
            qualifier : String?,
            keyword: String,
            val functionName: String,
            val parameters: List<ConstructorParameter> = emptyList(),
            bindings: List<KSDeclaration>
        ) : Definition(packageName, qualifier,keyword, bindings) {

            class Single(
                packageName: String,
                qualifier : String?,
                functionName: String,
                functionParameters: List<ConstructorParameter> = emptyList(),
                val createdAtStart : Boolean = false,
                bindings: List<KSDeclaration>
            ) : FunctionDeclarationDefinition(packageName, qualifier,"single", functionName, functionParameters, bindings)

            open class Factory(
                packageName: String,
                qualifier : String?,
                functionName: String,
                functionParameters: List<ConstructorParameter> = emptyList(),
                bindings: List<KSDeclaration>,
                _keyword : String = "factory"
            ) : FunctionDeclarationDefinition(packageName, qualifier,_keyword, functionName, functionParameters, bindings)

            class ViewModel(
                packageName: String,
                qualifier : String?,
                functionName: String,
                functionParameters: List<ConstructorParameter> = emptyList(),
                bindings: List<KSDeclaration>,
                _keyword : String = "org.koin.androidx.viewmodel.dsl.viewModel"
            ) : Factory(packageName, qualifier, functionName, functionParameters, bindings, _keyword)
        }

        sealed class ClassDeclarationDefinition(
            packageName: String,
            qualifier : String?,
            keyword: String,
            val className: String,
            val constructorParameters: List<ConstructorParameter> = emptyList(),
            bindings: List<KSDeclaration>,
        ) : Definition(packageName, qualifier, keyword,bindings) {

            class Single(
                packageName: String,
                qualifier : String?,
                className: String,
                constructorParameters: List<ConstructorParameter> = emptyList(),
                val createdAtStart : Boolean,
                bindings: List<KSDeclaration>
            ) : ClassDeclarationDefinition(packageName, qualifier,"single", className, constructorParameters, bindings)

            open class Factory(
                packageName: String,
                qualifier : String?,
                className: String,
                constructorParameters: List<ConstructorParameter> = emptyList(),
                bindings: List<KSDeclaration>,
                _keyword : String = "factory",
                _isAndroidDefinition : Boolean = false
            ) : ClassDeclarationDefinition(packageName, qualifier,_keyword, className, constructorParameters, bindings)

            class ViewModel(
                packageName: String,
                qualifier : String?,
                className: String,
                constructorParameters: List<ConstructorParameter> = emptyList(),
                bindings: List<KSDeclaration>,
                _keyword : String = "org.koin.androidx.viewmodel.dsl.viewModel",
            ) : Factory(packageName, qualifier, className, constructorParameters, bindings, _keyword)
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