package metadata

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation

fun KSAnnotated.getDefinitionAnnotation(): Pair<String, KSAnnotation>? {
    return try {
        val a = annotations.firstOrNull { a -> KoinDefinitionAnnotation.isValidAnnotation(a.shortName.asString()) }
        a?.let { Pair(a.shortName.asString(),a) }
    } catch (e: Exception) {
        null
    }
}