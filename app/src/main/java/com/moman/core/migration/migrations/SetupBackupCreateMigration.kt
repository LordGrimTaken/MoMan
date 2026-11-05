package com.moman.core.migration.migrations

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.data.backup.create.BackupCreateWorker
import com.moman.core.migration.Migration
import com.moman.core.migration.MigrationContext

@Inject
@ContributesIntoSet(AppScope::class)
class SetupBackupCreateMigration(
    private val context: Context,
) : Migration {
    override val version: Float = Migration.ALWAYS

    override suspend fun invoke(migrationContext: MigrationContext): Boolean {
        BackupCreateWorker.setupTask(context)
        return true
    }
}
