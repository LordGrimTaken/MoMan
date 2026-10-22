package moman.domain.extension.interactor

import dev.zacsweers.metro.Inject
import moman.domain.extension.repository.ExtensionStoreRepository

@Inject
class GetExtensionStoreCountAsFlow(
    private val repository: ExtensionStoreRepository,
) {
    operator fun invoke() = repository.getCountAsFlow()
}
