package metadata

import org.koin.core.annotation.*
import kotlin.reflect.KClass

val DEFINITION_ANNOTATION_LIST = listOf<KClass<*>>(
    Single::class,
    Factory::class,
    ViewModel::class
)

enum class DefinitionAnnotation {
    Single,
    Factory,
    ViewModel
    ;

    companion object {
        private val allValues : List<String> = values().map { it.toString() }
        fun isValidAnnotation(s : String) : Boolean = s in allValues
    }
}
