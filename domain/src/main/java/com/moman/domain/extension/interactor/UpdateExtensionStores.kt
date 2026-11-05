package com.moman.domain.extension.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.extension.repository.ExtensionStoreRepository

@Inject
class UpdateExtensionStores(
    private val repository: ExtensionStoreRepository,
) {
    suspend operator fun invoke() {
        repository.refreshAll()
    }
}
