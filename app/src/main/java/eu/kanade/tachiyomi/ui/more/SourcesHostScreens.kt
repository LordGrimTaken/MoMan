package eu.kanade.tachiyomi.ui.more

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.AppBarTitle
import eu.kanade.presentation.components.SearchToolbar
import eu.kanade.presentation.components.TabContent
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.browse.extension.ExtensionsViewModel
import eu.kanade.tachiyomi.ui.browse.extension.extensionsTab
import eu.kanade.tachiyomi.ui.browse.migration.sources.migrateSourceTab
import eu.kanade.tachiyomi.ui.browse.source.sourcesTab
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource

/**
 * Hosts one of the former Browse tabs as a stand-alone screen with its own top bar and back button.
 */
@Composable
private fun TabHostScaffold(
    title: String,
    tab: TabContent,
    navigateUp: () -> Unit,
    searchQuery: String? = null,
    onChangeSearchQuery: (String?) -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = { scrollBehavior ->
            SearchToolbar(
                titleContent = { AppBarTitle(title) },
                navigateUp = navigateUp,
                searchEnabled = tab.searchEnabled,
                searchQuery = if (tab.searchEnabled) searchQuery else null,
                onChangeSearchQuery = onChangeSearchQuery,
                actions = { AppBarActions(tab.actions) },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        tab.content(contentPadding, snackbarHostState)
    }
}

class MySourcesScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TabHostScaffold(
            title = stringResource(MR.strings.you_my_sources),
            tab = sourcesTab(),
            navigateUp = navigator::pop,
        )
    }
}

class ExtensionsHostScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val extensionsViewModel = metroViewModel<ExtensionsViewModel>()
        val searchQuery by extensionsViewModel.searchQuery.collectAsStateWithLifecycle()
        TabHostScaffold(
            title = stringResource(MR.strings.label_extensions),
            tab = extensionsTab(extensionsViewModel),
            navigateUp = navigator::pop,
            searchQuery = searchQuery,
            onChangeSearchQuery = extensionsViewModel::search,
        )
    }
}

class MigrateHostScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TabHostScaffold(
            title = stringResource(MR.strings.label_migration),
            tab = migrateSourceTab(),
            navigateUp = navigator::pop,
        )
    }
}
