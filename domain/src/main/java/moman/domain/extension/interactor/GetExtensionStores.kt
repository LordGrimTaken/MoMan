package moman.domain.extension.interactor

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import moman.domain.extension.model.ExtensionStore
import moman.domain.extension.repository.ExtensionStoreRepository

@Inject
class GetExtensionStores(
    private val repository: ExtensionStoreRepository,
) {
    suspend fun get(): List<ExtensionStore> = repository.getAll()

    fun subscribe(): Flow<List<ExtensionStore>> = repository.getAllAsFlow()
}
