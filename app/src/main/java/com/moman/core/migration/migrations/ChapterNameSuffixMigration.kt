package com.moman.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import com.moman.core.migration.Migration
import com.moman.core.migration.MigrationContext
import com.moman.domain.library.service.LibraryPreferences

@Inject
@ContributesIntoSet(AppScope::class)
class ChapterNameSuffixMigration(
    private val preference: LibraryPreferences,
) : Migration {
    override val version: Float = 34f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        if (migrationContext.previousVersion in 13..33) {
            preference.enableChapterNameHash.set(true)
        }
        return true
    }
}
