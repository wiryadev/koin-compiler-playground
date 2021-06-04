import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSTypeReference

sealed class KoinMetaData {

    data class Module(
        val packageName: String,
        val fieldName: String,
        val definitions: MutableList<Definition> = mutableListOf()
    ) : KoinMetaData()

    sealed class Definition(
        val packageName: String,
        val className: String,
        val constructorParameters: List<ConstructorParameter> = emptyList(),
        val bindings: List<KSTypeReference>
    ) : KoinMetaData() {

        class Single(
            packageName: String,
            className: String,
            constructorParameters: List<ConstructorParameter> = emptyList(),
            bindings: List<KSTypeReference>
        ) : Definition(packageName, className, constructorParameters, bindings)
    }

    data class ConstructorParameter(
        val qualifier : String? = null,
        val type : ConstructorParameterType = ConstructorParameterType.DEPENDENCY
    )
    enum class ConstructorParameterType {
        DEPENDENCY, PARAMETER_INJECT
    }
}