package eu.kanade.tachiyomi.ui.today

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.history.interactor.GetHistory
import tachiyomi.domain.history.interactor.GetNextChapters
import tachiyomi.domain.manga.interactor.GetLibraryManga
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.domain.manga.model.asMangaCover
import tachiyomi.domain.updates.interactor.GetUpdates
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
class TodayViewModel(
    private val getHistory: GetHistory,
    private val getNextChapters: GetNextChapters,
    private val getLibraryManga: GetLibraryManga,
    private val getUpdates: GetUpdates,
) : ViewModel() {

    private val resume: Flow<ResumeItem?> = getHistory.subscribe("")
        .map { list ->
            list.firstOrNull()?.let {
                ResumeItem(
                    mangaId = it.mangaId,
                    chapterId = it.chapterId,
                    title = it.title,
                    chapterNumber = it.chapterNumber
                        .takeIf { number -> number >= 0.0 }
                        ?.let { number ->
                            if (number % 1.0 == 0.0) number.toLong().toString() else number.toString()
                        },
                    cover = it.coverData,
                )
            }
        }
        .distinctUntilChanged()
        .catch {
            logcat(LogPriority.ERROR, it)
            emit(null)
        }
        .flowOn(Dispatchers.Default)

    private val dropped: Flow<List<DroppedItem>> = flow {
            val now = Clock.System.now()
            val cutoff = now - 24.hours
            // The query filters on upload date; widen it and narrow by fetch time below.
            val queryAfter = now.minus(7, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            emitAll(getUpdates.subscribe(read = false, after = queryAfter).map { updates ->
                updates
                    .filter { it.dateFetch >= cutoff }
                    .groupBy { it.mangaId }
                    .map { (mangaId, items) ->
                        DroppedItem(
                            mangaId = mangaId,
                            title = items.first().mangaTitle,
                            cover = items.first().coverData,
                            unreadCount = items.size,
                        )
                    }
            })
        }
        .catch {
            logcat(LogPriority.ERROR, it)
            emit(emptyList())
        }
        .flowOn(Dispatchers.Default)

    private val upNext: Flow<List<UpNextItem>> = getLibraryManga.subscribe()
        .map { library ->
            library
                .filter { it.unreadCount > 0 && it.lastRead != null }
                .sortedByDescending { it.lastRead }
                .take(10)
                .map {
                    UpNextItem(
                        mangaId = it.manga.id,
                        title = it.manga.title,
                        cover = it.manga.asMangaCover(),
                        unreadCount = it.unreadCount,
                    )
                }
        }
        .catch {
            logcat(LogPriority.ERROR, it)
            emit(emptyList())
        }
        .flowOn(Dispatchers.Default)

    val state: StateFlow<State> = combine(resume, dropped, upNext) { resume, dropped, upNext ->
        State(loading = false, resume = resume, dropped = dropped, upNext = upNext)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds), State())

    /**
     * Next chapter to read for the given history entry (the same chapter if it isn't fully read).
     */
    suspend fun getNextChapter(mangaId: Long, chapterId: Long): Chapter? {
        return getNextChapters.await(mangaId, chapterId, onlyUnread = false).firstOrNull()
    }

    @Immutable
    data class State(
        val loading: Boolean = true,
        val resume: ResumeItem? = null,
        val dropped: List<DroppedItem> = emptyList(),
        val upNext: List<UpNextItem> = emptyList(),
    )

    @Immutable
    data class ResumeItem(
        val mangaId: Long,
        val chapterId: Long,
        val title: String,
        val chapterNumber: String?,
        val cover: MangaCover,
    )

    @Immutable
    data class DroppedItem(
        val mangaId: Long,
        val title: String,
        val cover: MangaCover,
        val unreadCount: Int,
    )

    @Immutable
    data class UpNextItem(
        val mangaId: Long,
        val title: String,
        val cover: MangaCover,
        val unreadCount: Long,
    )
}
