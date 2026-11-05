package com.moman.domain.category.interactor

import dev.zacsweers.metro.Inject
import com.moman.domain.library.model.LibraryDisplayMode
import com.moman.domain.library.service.LibraryPreferences

@Inject
class SetDisplayMode(
    private val preferences: LibraryPreferences,
) {

    fun await(display: LibraryDisplayMode) {
        preferences.displayMode.set(display)
    }
}
