import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import org.koin.core.annotation.Single
import java.io.OutputStream

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}
class BuilderProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Single::class.qualifiedName!!)
        val ret = symbols.filter { !it.validate() }.toList()
        val all = symbols.filter { it is KSClassDeclaration && it.validate() }
        val count = all.count()
        all.forEachIndexed { index, ksAnnotated -> ksAnnotated.accept(BuilderVisitor(index == 0, index == count -1), Unit) }
        return ret
    }

    inner class BuilderVisitor(val isFirst : Boolean, val isLast : Boolean) : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            classDeclaration.primaryConstructor!!.accept(this, data)
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val ksClassDeclaration = function.parentDeclaration as KSClassDeclaration
            val packageName = ksClassDeclaration.containingFile!!.packageName.asString()
            val className = ksClassDeclaration.simpleName.asString()
            val file = codeGenerator.createNewFile(Dependencies.ALL_FILES, "org.koin.ksp.generated" , "Default") //TODO Incremental here?
            val paramsCount = ksClassDeclaration.primaryConstructor?.parameters?.count() ?: 0
            val ctor = if (paramsCount > 0) (1..paramsCount).joinToString(",",prefix = "(",postfix = ")") { "get()" } else "()"
            val interfaces = ksClassDeclaration.superTypes.map {
                val ksType = it.resolve()
                ksType.declaration.packageName.asString() +"."+ ksType.declaration.simpleName.asString()
            }.joinToString { "bind $it::class" }
            if (isFirst){
                file.appendText("""
                    package org.koin.ksp.generated

                    import org.koin.dsl.module
                    import org.koin.dsl.bind

                    val defaultModule = module {
                """.trimIndent())
                file.appendText("\n")
            }
            file.appendText("\tsingle { $packageName.$className$ctor } $interfaces\n")
            if (isLast){
                file.appendText("""
                    }
                """.trimIndent())
            }
//            file.appendText("import HELLO\n\n")
//            file.appendText("class $className{\n")
//            function.parameters.forEach {
//                val name = it.name!!.asString()
//                val typeName = StringBuilder(it.type.resolve().declaration.qualifiedName?.asString() ?: "<ERROR>")
//                val typeArgs = it.type.element!!.typeArguments
//                if (it.type.element!!.typeArguments.isNotEmpty()) {
//                    typeName.append("<")
//                    typeName.append(
//                            typeArgs.map {
//                                val type = it.type?.resolve()
//                                "${it.variance.label} ${type?.declaration?.qualifiedName?.asString() ?: "ERROR"}" +
//                                        if (type?.nullability == Nullability.NULLABLE) "?" else ""
//                            }.joinToString(", ")
//                    )
//                    typeName.append(">")
//                }
//                file.appendText("    private var $name: $typeName? = null\n")
//                file.appendText("    internal fun with${name.capitalize()}($name: $typeName): $className {\n")
//                file.appendText("        this.$name = $name\n")
//                file.appendText("        return this\n")
//                file.appendText("    }\n\n")
//            }
//            file.appendText("    internal fun build(): ${parent.qualifiedName!!.asString()} {\n")
//            file.appendText("        return ${parent.qualifiedName!!.asString()}(")
//            file.appendText(
//                function.parameters.map {
//                    "${it.name!!.asString()}!!"
//                }.joinToString(", ")
//            )
//            file.appendText(")\n")
//            file.appendText("    }\n")
//            file.appendText("}\n")
        }
    }

}

class BuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return BuilderProcessor(env.codeGenerator, env.logger)
    }
}