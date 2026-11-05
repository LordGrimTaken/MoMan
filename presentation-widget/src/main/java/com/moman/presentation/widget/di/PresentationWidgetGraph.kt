package com.moman.presentation.widget.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import com.moman.presentation.widget.BaseUpdatesGridGlanceWidget

@ContributesTo(AppScope::class)
interface PresentationWidgetGraph {
    fun inject(widget: BaseUpdatesGridGlanceWidget)
}
