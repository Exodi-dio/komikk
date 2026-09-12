package io.komikk.registry.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier

/**
 * KSP entry point. Registers the processor via
 * `META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`.
 */
class SourceRegistryProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        SourceRegistryProcessor(environment)
}

/**
 * Scans a variant's `@KomikkSource` annotations and emits a
 * [RegistrySourceBuilder.REGISTRY_FILE] implementing
 * `io.komikk.reader.sources.SourceRegistry` for that variant.
 *
 * Generation always happens (even with zero sources) so every variant compiles
 * against the same `GeneratedSourceRegistry` shape.
 */
class SourceRegistryProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (generated) return emptyList()
        generated = true

        val annotated = resolver.getSymbolsWithAnnotation(ANNOTATION).toList()
        val specs = mutableListOf<SourceSpec>()
        val seenIds = mutableSetOf<String>()

        for (symbol in annotated) {
            val declaration = symbol as? KSClassDeclaration
            if (declaration == null) {
                environment.logger.error("@KomikkSource is only valid on classes", symbol)
                continue
            }
            reportValidation(declaration)?.let { message ->
                environment.logger.error(message, declaration)
                continue
            }
            val spec = toSpec(declaration)
            if (!seenIds.add(spec.id)) {
                environment.logger.error(
                    "Duplicate KomikkSource id '${spec.id}' (ids must be unique and stable)",
                    declaration,
                )
                continue
            }
            specs += spec
        }

        val output = environment.codeGenerator.createNewFile(
            Dependencies(false),
            RegistrySourceBuilder.REGISTRY_PACKAGE,
            RegistrySourceBuilder.REGISTRY_FILE,
            "kt",
        )
        output.write(RegistrySourceBuilder.build(specs).toByteArray())
        output.close()
        return emptyList()
    }

    private fun toSpec(declaration: KSClassDeclaration): SourceSpec {
        val annotation = declaration.annotations
            .first { it.annotationType.resolve().declaration.qualifiedName?.asString() == ANNOTATION }
        fun arg(name: String): Any? =
            annotation.arguments.firstOrNull { it.name?.asString() == name }?.value
        return SourceSpec(
            id = arg("id") as String? ?: error("missing id"),
            name = arg("name") as String? ?: error("missing name"),
            lang = arg("lang") as String? ?: DEFAULT_LANG,
            version = arg("version") as Int? ?: error("missing version"),
            className = declaration.qualifiedName?.asString()
                ?: error("source class has no qualified name"),
        )
    }

    private fun reportValidation(declaration: KSClassDeclaration): String? {
        if (declaration.classKind != ClassKind.CLASS) {
            return "KomikkSource must target a class (got ${declaration.classKind})"
        }
        if (Modifier.ABSTRACT in declaration.modifiers) {
            return "KomikkSource class must not be abstract"
        }
        if (!isEffectivelyPublic(declaration.modifiers)) {
            return "KomikkSource class must be public (registry calls it from the app)"
        }
        val constructor = declaration.getPrimaryConstructor()
            ?: return "KomikkSource class must declare a single primary constructor"
        if (!isEffectivelyPublic(constructor.modifiers)) {
            return "KomikkSource constructor must be public"
        }
        val parameterTypes = constructor.parameters
            .map { it.type.resolve().declaration.qualifiedName?.asString() }
        if (parameterTypes != listOf(HTTP_EXECUTOR, SOURCE_PREFERENCES)) {
            return "KomikkSource constructor must be (SourceHttpExecutor, SourcePreferences)"
        }
        return null
    }

    /**
     * KSP idiom for "is this effectively public?". `Modifier.PUBLIC` is not
     * emitted for implicit (default) public, so public = no visibility
     * restriction is present. Same shape as the verified
     * `Modifier.ABSTRACT in declaration.modifiers` check in [reportValidation].
     */
    private fun isEffectivelyPublic(modifiers: Set<Modifier>): Boolean =
        Modifier.PRIVATE !in modifiers &&
            Modifier.PROTECTED !in modifiers &&
            Modifier.INTERNAL !in modifiers

    private companion object {
        const val ANNOTATION = "io.komikk.core.KomikkSource"
        const val HTTP_EXECUTOR = "io.komikk.core.SourceHttpExecutor"
        const val SOURCE_PREFERENCES = "io.komikk.core.SourcePreferences"
        const val DEFAULT_LANG = "en"
    }
}