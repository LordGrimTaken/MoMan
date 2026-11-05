package com.moman.data.source

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import com.moman.data.Database
import com.moman.data.subscribeToList
import com.moman.domain.source.model.StubSource
import com.moman.domain.source.repository.StubSourceRepository

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class StubSourceRepositoryImpl(
    private val database: Database,
) : StubSourceRepository {

    override fun subscribeAll(): Flow<List<StubSource>> {
        return database.sourceQueries
            .findAll(::mapStubSource)
            .subscribeToList()
    }

    override suspend fun getStubSource(id: Long): StubSource? {
        return database.sourceQueries
            .findOne(id, ::mapStubSource)
            .awaitAsOneOrNull()
    }

    override suspend fun upsertStubSource(id: Long, lang: String, name: String) {
        database.sourceQueries.upsert(id = id, name = name, language = lang)
    }

    private fun mapStubSource(
        id: Long,
        name: String,
        lang: String,
    ): StubSource = StubSource(id = id, lang = lang, name = name)
}
