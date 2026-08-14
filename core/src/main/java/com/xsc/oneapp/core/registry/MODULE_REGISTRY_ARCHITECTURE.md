# Module Registry

## Why this exists

Before this refactor, a business feature module's identity was scattered across three
places that had to be kept in sync by convention, not by the compiler:

- **Dashboard** knew which modules existed only in the sense of rendering whatever
  `accessibleModules` returned - it never had its own notion of "what modules does
  this app know how to show."
- **`RootNavHost` / `Routes.destinationFor`** owned a hardcoded `when` block mapping
  backend route strings ("fees", "exam", "academics") to internal navigation
  destinations, duplicated once per module, with no single place that listed "these
  are the modules this app has."
- **Each feature module** knew its own navigation entry point (`AttendanceDestinations`,
  `ProfileDestinations`, or a bare route string for the simpler modules) but had no way
  to declare that fact anywhere outside its own package.

None of these three ever cross-checked each other. A new module needed its route added
to `Routes.kt`'s `when` block by hand, in a file the module itself doesn't own, with
nothing enforcing that the string matched what the module's own navigation code
actually expected.

The `ModuleRegistry` replaces that with one place a module states what it is, and one
place everything else reads from - and it does so with something already present in
the codebase (Hilt multibinding, the same pattern `DashboardStatProvider` already
uses), not a new mechanism.

## What it deliberately does not do

Two things worth being explicit about, because they're easy to assume incorrectly:

**It doesn't decide who can see a module.** That's still `accessibleModules` (server)
feeding `MainViewModel.accessibleRoutes` (client), consulted via
`UiState<Set<String>>.deniesRoute()`. The registry answers "what is this module and
how do I navigate to it," never "can this session see it." Keeping these separate
matters: access is dynamic and per-session, identity is static and compiled in. Merging
them would mean either the registry needing network access it has no business needing,
or access decisions being baked into a build.

**It doesn't own display metadata.** `displayName` and `iconMetadata` exist on
`ModuleDefinition` for future tooling/documentation use, but Dashboard does not read
them - it still renders whatever the backend's `accessibleModules` response says
(label, icon, colour, status). That's deliberate: an institution can already customise
a module's displayed name server-side without an app release, and switching Dashboard
to this registry's static strings would quietly regress that. If Dashboard ever needs
a registry-owned fallback label (e.g., for a module the backend hasn't described yet),
that's a real decision to make explicitly later, not something to slide in as a side
effect of this refactor.

## How a module registers

A feature module contributes exactly one `ModuleDefinition` via `@Provides @IntoSet`
in its own existing Hilt module's companion object - see `AttendanceModule`,
`ProfileModule`, `ExamModule`, `FeeModule`, `CurriculumModule`, `TimetableModule`.
`ModuleRegistryModule` (in `:app`) declares the `@Multibinds` fallback so
`Set<ModuleDefinition>` resolves to an empty set if a module hasn't contributed yet,
then wraps the aggregated set in an injectable `ModuleRegistry`.

No reflection, no `ServiceLoader`, no runtime classpath scanning - the set is assembled
by Hilt at compile time, the same way `Set<DashboardStatProvider>` already is.

`:core` itself has no Dagger/Hilt dependency at all. `ModuleDefinition` and
`ModuleRegistry` are plain Kotlin - the multibinding wiring lives entirely in `:app`,
which is already the aggregation point for every feature module.

## How later phases use this without redesigning it

**Capability Resolution** (deciding which screens/actions within an already-accessible
module a given role sees) will need to know a module's identity and its fine-grained
permission requirements. `ModuleDefinition.requiredPermissions` exists for this today
as an always-empty placeholder - not because the field was guessed at, but because the
client genuinely has no source for a real per-module permission string yet (see the
RBAC audit's B-6/B-7 findings: `accessibleModules` is already filtered server-side and
never returns the permission it matched on, and permissions are derived per-action,
`mod.subMod.action.actionType`, not per-module). When that mapping becomes available -
whether from the backend's Permission Registry or elsewhere - it populates this field
without changing `ModuleDefinition`'s shape or any code that already reads a
`ModuleRegistry`.

**Dynamic Feature Delivery** needs to know, per module, whether it ships in the base
app or gets installed on demand, and (eventually) what its dynamic-feature Gradle
module is called. `ModuleDefinition.isBaseModule`, `.installPolicy`, and
`.dynamicFeatureName` exist today with fixed values - every module is `isBaseModule =
true`, `installPolicy = InstallPolicy.BASE`, `dynamicFeatureName = null`, because
nothing in OneApp is a dynamic feature yet. Converting a module later means changing
that one module's own `ModuleDefinition` contribution to non-default values - not
introducing a new field, and not touching `Dashboard`, `RootNavHost`, or any other
module's registration.

## What actually changed in this refactor

`Routes.destinationFor(route: String)` became `destinationFor(route: String, registry:
ModuleRegistry)` - the alias-to-destination `when` block was replaced by
`registry.findByRouteAlias(normalizedKey)`, with the exact same normalisation (trim,
strip surrounding slashes, lowercase) and the exact same fallback to the generic
`module(key)` template for anything unrecognised. `MainViewModel` now injects
`ModuleRegistry` and passes it through; `RootNavHost` exposes it from `MainViewModel`
the same way it already exposes `sessionManager`. No screen, no access-control check,
and no user-visible behaviour changed - `RoutesTest` pins down that every previously
supported alias still resolves identically.
