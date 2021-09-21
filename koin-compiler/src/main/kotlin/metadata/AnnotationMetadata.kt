package metadata

import org.koin.core.annotation.*
import kotlin.reflect.KClass

data class DefinitionAnnotation(
    val name : String,
    val import : String? = null,
    val annotationType : KClass<*>
)

val SINGLE = DefinitionAnnotation("single",annotationType = Single::class)
val FACTORY = DefinitionAnnotation("factory",annotationType = Factory::class)
val VIEWMODEL = DefinitionAnnotation("viewModel","org.koin.androidx.viewmodel.dsl.viewModel", ViewModel::class)

val DEFINITION_ANNOTATION_LIST = listOf(SINGLE, FACTORY, VIEWMODEL)

val DEFINITION_ANNOTATION_LIST_NAMES = DEFINITION_ANNOTATION_LIST.map { it.name }
val DEFINITION_ANNOTATION_LIST_TYPES = DEFINITION_ANNOTATION_LIST.map { it.annotationType }

fun isValidAnnotation(s : String) : Boolean = s.toLowerCase() in DEFINITION_ANNOTATION_LIST_NAMES

