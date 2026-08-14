package com.xsc.oneapp.core.registry

/**
 * The base app's install classification for a module, per [ModuleDefinition.installPolicy].
 * Every module is [BASE] today - nothing in OneApp is delivered on demand yet. This
 * exists now so a future Dynamic Feature Delivery phase has a field to flip a module
 * to [ON_DEMAND] without redesigning anything that already consults the registry.
 */
enum class InstallPolicy { BASE, ON_DEMAND }

/**
 * The single source of truth for a business feature module's static, code-level
 * identity - what it's called internally, how to navigate to it, and (for future
 * phases) whether it ships in the base app or on demand. A feature module contributes
 * exactly one of these via `@Provides @IntoSet` in its own Hilt module (see
 * AttendanceModule, ProfileModule, ExamModule, FeeModule, CurriculumModule,
 * TimetableModule) - mirrors the existing DashboardStatProvider multibinding pattern,
 * no reflection or ServiceLoader involved. See [ModuleRegistry] for how these get
 * consulted.
 *
 * What this deliberately does NOT own: [displayName] and [iconMetadata] exist for
 * future tooling/documentation use only and are never consulted by Dashboard today.
 * The rendered tile's label, icon, colour and status all come from the backend's
 * `accessibleModules` response (see feature/dashboard's ModuleItem) and stay that way
 * on purpose - an institution can already customise those server-side without an app
 * release, and wiring Dashboard to this class's static fields instead would silently
 * regress that. Likewise, whether a signed-in session can actually reach a module is
 * still decided entirely by the same backend-driven accessible-routes set as before
 * (see MainViewModel.accessibleRoutes / core.result.deniesRoute) - this registry only
 * answers "what is this module and how do I navigate to it," never "can this session
 * see it."
 *
 * [requiredPermissions] and [dynamicFeatureName] are placeholders, not working
 * mechanisms, for two different reasons documented on each field below - both are
 * empty/null for every module today.
 */
data class ModuleDefinition(
    val moduleId: String,
    val displayName: String,
    val navigationEntry: String,
    /**
     * Every normalised route key that should resolve to [navigationEntry] - e.g.
     * exams accepts both "exams" and "exam" (see Routes.destinationFor's original
     * doc comment on why singular aliases and the academics/curriculum rename are
     * both accepted). This field only exists to reproduce that exact multi-alias
     * behaviour through the registry; it wasn't part of the original field list.
     */
    val routeAliases: Set<String>,
    /**
     * Always empty today. The client has no source for a real per-module permission
     * string - the backend's `accessibleModules` response is already filtered
     * server-side before the client ever sees it, and permissions are derived
     * per-action (`mod.subMod.action.actionType`), not per-module, so there isn't a
     * single string to put here even in principle (see the RBAC audit's B-6/B-7
     * findings). Left in the shape now so a future Capability Resolver phase has a
     * field to populate once/if the backend exposes that mapping - not populated
     * speculatively before that exists.
     */
    val requiredPermissions: List<String> = emptyList(),
    /** Reserved for future tooling/documentation. Not consulted by Dashboard - see class doc. */
    val iconMetadata: String? = null,
    val isBaseModule: Boolean = true,
    /**
     * Always null today - nothing in OneApp is a dynamic feature yet. Reserved so a
     * future Dynamic Feature Delivery phase has a field to populate per module
     * without changing this class's shape or anything that already consumes it.
     */
    val dynamicFeatureName: String? = null,
    val installPolicy: InstallPolicy = InstallPolicy.BASE
)
