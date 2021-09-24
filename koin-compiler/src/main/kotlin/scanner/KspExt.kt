package metadata

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import generator.KoinCodeGenerator
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Property
import org.koin.core.annotation.Qualifier

fun KSAnnotated.getDefinitionAnnotation(): Pair<String, KSAnnotation>? {
    return try {
        val a = annotations.firstOrNull { a -> isValidAnnotation(a.shortName.asString()) }
        a?.let { Pair(a.shortName.asString(),a) }
    } catch (e: Exception) {
        null
    }
}

fun KSAnnotated. getStringQualifier(): String? {
    val qualifierAnnotation = annotations.firstOrNull { a -> a.shortName.asString() == "Qualifier" }
    return qualifierAnnotation?.let {
        qualifierAnnotation.arguments.getValueArgument() ?: error("Can't get value for @Qualifier")
    }
}

fun List<KSValueParameter>.getConstructorParameters() : List<KoinMetaData.ConstructorParameter>{
    return map { param -> getConstructorParameter(param) }
}

private fun getConstructorParameter(param: KSValueParameter): KoinMetaData.ConstructorParameter {
    val firstAnnotation = param.annotations.firstOrNull()
    val annotationName = firstAnnotation?.shortName?.asString()
    val annotationValue = firstAnnotation?.arguments?.getValueArgument()
    KoinCodeGenerator.LOGGER.warn("annotation? $firstAnnotation  name:$annotationName  value:$annotationValue")
    return when(annotationName){
        "${InjectedParam::class.simpleName}" -> KoinMetaData.ConstructorParameter.ParameterInject
        "${Property::class.simpleName}" -> KoinMetaData.ConstructorParameter.Property(annotationValue)
        "${Qualifier::class.simpleName}" -> KoinMetaData.ConstructorParameter.Dependency(annotationValue)
        else -> KoinMetaData.ConstructorParameter.Dependency()
    }
}

private fun List<KSValueArgument>.getValueArgument(): String? {
    return firstOrNull { a -> a.name?.asString() == "value" }?.value as? String?
}
