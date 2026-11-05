package com.moman.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import com.moman.domain.source.service.SourcePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.LogPriority
import com.moman.core.migration.Migration
import com.moman.core.migration.MigrationContext
import com.moman.domain.extension.repository.ExtensionStoreRepository
import com.moman.core.common.util.system.logcat

@Inject
@ContributesIntoSet(AppScope::class)
class TrustExtensionRepositoryMigration(
    private val sourcePreferences: SourcePreferences,
    private val repository: ExtensionStoreRepository,
) : Migration {
    override val version: Float = 7f

    override suspend fun invoke(migrationContext: MigrationContext): Boolean = withContext(Dispatchers.IO) {
        for ((index, source) in sourcePreferences.extensionRepos.get().withIndex()) {
            try {
                repository.insertFromPreference(
                    indexUrl = source.removeSuffix("/index.min.json").removeSuffix("/index.json") + "/repo.json",
                    name = "Repo #${index + 1}",
                )
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e) { "Error Migrating Extension Repo with baseUrl: $source" }
            }
        }
        sourcePreferences.extensionRepos.delete()
        return@withContext true
    }
}
