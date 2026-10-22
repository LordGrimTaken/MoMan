package eu.kanade.tachiyomi.ui.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metrox.viewmodel.metroViewModel
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.settings.screen.browse.ExtensionStoreDialog
import eu.kanade.presentation.more.settings.screen.browse.ExtensionStoreEvent
import eu.kanade.presentation.more.settings.screen.browse.ExtensionStoreScreenState
import eu.kanade.presentation.more.settings.screen.browse.ExtensionStoresViewModel
import eu.kanade.presentation.more.settings.screen.browse.components.ExtensionStoreConfirmDialog
import eu.kanade.presentation.more.settings.screen.browse.components.ExtensionStoreCreateDialog
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.home.HomeScreen
import kotlinx.coroutines.launch
import tachiyomi.core.common.i18n.stringResource as contextStringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource

/**
 * Friendly four-step guide to adding a repository and installing a first source.
 */
class AddSourcesScreen : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        val viewModel = metroViewModel<ExtensionStoresViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                val message = when (event) {
                    ExtensionStoreEvent.Added -> context.contextStringResource(MR.strings.import_repo_added)
                    is ExtensionStoreEvent.AddFailed ->
                        context.contextStringResource(MR.strings.import_repo_add_failed, event.message)
                }
                snackbarHostState.showSnackbar(message)
            }
        }

        Scaffold(
            topBar = { scrollBehavior ->
                AppBar(
                    title = stringResource(MR.strings.addsources_title),
                    navigateUp = navigator::pop,
                    scrollBehavior = scrollBehavior,
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { contentPadding ->
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    bottom = contentPadding.calculateBottomPadding() + 32.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "intro") {
                    Text(
                        text = stringResource(MR.strings.addsources_intro),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item(key = "s1") {
                    StepCard(
                        number = 1,
                        title = stringResource(MR.strings.addsources_step1_title),
                        body = stringResource(MR.strings.addsources_step1_body),
                    )
                }
                item(key = "s2") {
                    StepCard(
                        number = 2,
                        title = stringResource(MR.strings.addsources_step2_title),
                        body = stringResource(MR.strings.addsources_step2_body),
                        buttonText = stringResource(MR.strings.addsources_step2_button),
                        primaryButton = true,
                        onButton = { viewModel.showDialog(ExtensionStoreDialog.Create()) },
                    )
                }
                item(key = "s3") {
                    StepCard(
                        number = 3,
                        title = stringResource(MR.strings.addsources_step3_title),
                        body = stringResource(MR.strings.addsources_step3_body),
                        buttonText = stringResource(MR.strings.addsources_step3_button),
                        onButton = { navigator.push(ExtensionsHostScreen()) },
                    )
                }
                item(key = "s4") {
                    StepCard(
                        number = 4,
                        title = stringResource(MR.strings.addsources_step4_title),
                        body = stringResource(MR.strings.addsources_step4_body),
                        buttonText = stringResource(MR.strings.addsources_step4_button),
                        onButton = {
                            scope.launch {
                                navigator.popUntilRoot()
                                HomeScreen.openTab(HomeScreen.Tab.Browse())
                            }
                        },
                    )
                }
                item(key = "legal") {
                    Text(
                        text = stringResource(MR.strings.addsources_legal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }

        val successState = state as? ExtensionStoreScreenState.Success
        when (val dialog = successState?.dialog) {
            is ExtensionStoreDialog.Create -> ExtensionStoreCreateDialog(
                onDismissRequest = viewModel::dismissDialog,
                onCreate = { viewModel.createRepo(it) },
                storeIndexUrls = successState.stores.map { it.indexUrl }.toSet(),
                processing = dialog.processing,
                errorMessage = dialog.errorMessage,
            )
            is ExtensionStoreDialog.Confirm -> ExtensionStoreConfirmDialog(
                onDismissRequest = viewModel::dismissDialog,
                onCreate = { viewModel.createRepo(dialog.url) },
                storeIndexUrl = dialog.url,
                storeAlreadyExists = dialog.alreadyExists,
                processing = dialog.processing,
                errorMessage = dialog.errorMessage,
            )
            else -> {}
        }
    }
}

@Composable
private fun StepCard(
    number: Int,
    title: String,
    body: String,
    buttonText: String? = null,
    primaryButton: Boolean = false,
    onButton: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SealNumber(number)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (buttonText != null) {
                    Spacer(Modifier.height(4.dp))
                    if (primaryButton) {
                        Button(onClick = onButton, shape = MaterialTheme.shapes.small) { Text(buttonText) }
                    } else {
                        OutlinedButton(
                            onClick = onButton,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        ) {
                            Text(text = buttonText, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SealNumber(number: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}
