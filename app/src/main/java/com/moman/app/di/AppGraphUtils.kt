package com.moman.app.di

import android.content.Context
import com.moman.core.metro.metroGraph

val Context.appGraph get() = metroGraph<AppGraph>()
