package moman.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.domain.ui.model.AppTheme
import moman.core.migration.Migration
import moman.core.migration.MigrationContext

/**
 * Early test builds defaulted to the system (Monet) colors and may have stored the AMOLED switch.
 * MoMan's identity is the "Ink & Seal" theme, so reset both once when moving to 1.0.2.
 */
@Inject
@ContributesIntoSet(AppScope::class)
class InkSealThemeMigration(
    private val uiPreferences: UiPreferences,
) : Migration {
    override val version: Float = 5f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        uiPreferences.appTheme.set(AppTheme.DEFAULT)
        uiPreferences.themeDarkAmoled.set(false)
        return true
    }
}
