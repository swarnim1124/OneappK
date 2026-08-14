package com.xsc.oneapp.core.registry

/**
 * Consultable wrapper around every [ModuleDefinition] contributed across the app.
 * Deliberately a plain class with no Dagger/Hilt annotations - :core has no DI
 * framework dependency today (see UiState, SectionLoader, CrashReporter) and this
 * keeps it that way. The actual Hilt wiring (the `@Multibinds` fallback and the
 * `@Provides` construction of this class from the aggregated `Set<ModuleDefinition>`)
 * lives in :app's ModuleRegistryModule instead, since :app is already the aggregation
 * point for every feature module's contributions.
 */
class ModuleRegistry(private val definitions: Set<ModuleDefinition>) {

    fun all(): List<ModuleDefinition> = definitions.toList()

    fun findById(moduleId: String): ModuleDefinition? =
        definitions.firstOrNull { it.moduleId == moduleId }

    /** [normalizedKey] must already be trimmed/lowercased - see Routes.destinationFor. */
    fun findByRouteAlias(normalizedKey: String): ModuleDefinition? =
        definitions.firstOrNull { normalizedKey in it.routeAliases }
}
