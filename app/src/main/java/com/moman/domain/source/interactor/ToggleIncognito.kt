package com.moman.domain.source.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.source.service.SourcePreferences
import com.moman.core.common.preference.getAndSet

@Inject
class ToggleIncognito(
    private val preferences: SourcePreferences,
) {
    fun await(extensions: String, enable: Boolean) {
        preferences.incognitoExtensions.getAndSet {
            if (enable) it.plus(extensions) else it.minus(extensions)
        }
    }
}
