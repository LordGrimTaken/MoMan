package moman.app.di

import android.content.Context
import moman.core.metro.metroGraph

val Context.appGraph get() = metroGraph<AppGraph>()
