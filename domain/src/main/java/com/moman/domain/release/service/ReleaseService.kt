package com.moman.domain.release.service

import com.moman.domain.release.interactor.GetApplicationRelease
import com.moman.domain.release.model.Release

interface ReleaseService {

    suspend fun latest(arguments: GetApplicationRelease.Arguments): Release?
}
