package com.moman.domain.source.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.source.service.SourcePreferences
import com.moman.core.common.preference.getAndSet

@Inject
class ToggleLanguage(
    val preferences: SourcePreferences,
) {

    fun await(language: String) {
        val isEnabled = language in preferences.enabledLanguages.get()
        preferences.enabledLanguages.getAndSet { enabled ->
            if (isEnabled) enabled.minus(language) else enabled.plus(language)
        }
    }
}
