package com.moman.domain.extension.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.extension.repository.ExtensionStoreRepository

@Inject
class RemoveExtensionStore(
    private val repository: ExtensionStoreRepository,
) {
    suspend operator fun invoke(indexUrl: String) {
        repository.remove(indexUrl)
    }
}
