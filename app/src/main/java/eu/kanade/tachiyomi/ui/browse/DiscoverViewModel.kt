package eu.kanade.tachiyomi.ui.browse

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import eu.kanade.domain.source.interactor.GetEnabledSources
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.LogPriority
import moman.domain.manga.model.toDomainManga
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.manga.interactor.NetworkToLocalManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.model.Pin
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.source.local.isLocal

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding = binding<ViewModel>())
class DiscoverViewModel(
    private val getEnabledSources: GetEnabledSources,
    private val sourceManager: SourceManager,
    private val networkToLocalManga: NetworkToLocalManga,
) : ViewModel() {

    val state: StateFlow<State>
        field = MutableStateFlow(State())

    private val requested = HashSet<Long>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getEnabledSources.subscribe()
                .catch { logcat(LogPriority.ERROR, it) }
                .collect { sources ->
                    val picked = sources
                        .asSequence()
                        .filter { !it.isLocal() && !it.isStub }
                        .distinctBy { it.id }
                        .sortedByDescending { Pin.Actual in it.pin }
                        .take(MAX_SOURCES)
                        .map { Header(it.id, it.name, it.lang) }
                        .toList()
                    state.update { current ->
                        current.copy(
                            isLoading = false,
                            sources = picked,
                            sections = current.sections.filterKeys { id -> picked.any { it.id == id } },
                        )
                    }
                    picked.forEach { load(it.id, force = false) }
                }
        }
    }

    fun load(sourceId: Long, force: Boolean) {
        synchronized(requested) {
            if (!force && sourceId in requested) return
            requested.add(sourceId)
        }
        state.update { it.copy(sections = it.sections + (sourceId to Section.Loading)) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = try {
                val source = sourceManager.get(sourceId)
                    ?: error("Source not available")
                if (!source.supportsLatest) error("Source does not support latest updates")
                val page = source.getLatestUpdates(1)
                val seen = hashSetOf<String>()
                val mangas = page.mangas
                    .map { it.toDomainManga(sourceId) }
                    .filter { seen.add(it.url) }
                    .take(MAX_ITEMS)
                    .let { networkToLocalManga(it) }
                Section.Loaded(mangas)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                logcat(LogPriority.ERROR, e)
                synchronized(requested) { requested.remove(sourceId) }
                Section.Error
            }
            state.update { it.copy(sections = it.sections + (sourceId to result)) }
        }
    }

    @Immutable
    data class Header(val id: Long, val name: String, val lang: String)

    sealed interface Section {
        data object Loading : Section
        data object Error : Section

        @Immutable
        data class Loaded(val mangas: List<Manga>) : Section
    }

    @Immutable
    data class State(
        val isLoading: Boolean = true,
        val sources: List<Header> = emptyList(),
        val sections: Map<Long, Section> = emptyMap(),
    ) {
        val isEmpty get() = !isLoading && sources.isEmpty()
    }

    companion object {
        private const val MAX_SOURCES = 6
        private const val MAX_ITEMS = 20
    }
}
