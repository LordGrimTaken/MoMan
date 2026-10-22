package eu.kanade.presentation.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.settings.widget.SwitchPreferenceWidget
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.tachiyomi.ui.more.DownloadQueueState
import moman.icons.materialsymbols.MaterialSymbols
import moman.icons.materialsymbols.automirroredrounded.Help
import moman.icons.materialsymbols.automirroredrounded.Label
import moman.icons.materialsymbols.rounded.Add
import moman.icons.materialsymbols.rounded.CloudOff
import moman.icons.materialsymbols.rounded.Code
import moman.icons.materialsymbols.rounded.Download
import moman.icons.materialsymbols.rounded.Eyeglasses2
import moman.icons.materialsymbols.rounded.Info
import moman.icons.materialsymbols.rounded.KeyboardArrowRight
import moman.icons.materialsymbols.rounded.Public
import moman.icons.materialsymbols.rounded.QueryStats
import moman.icons.materialsymbols.rounded.Settings
import moman.icons.materialsymbols.rounded.Storage
import moman.icons.materialsymbols.rounded.SwapCalls
import tachiyomi.core.common.Constants
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun MoreScreen(
    downloadQueueStateProvider: () -> DownloadQueueState,
    downloadedOnly: Boolean,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    incognitoMode: Boolean,
    onIncognitoModeChange: (Boolean) -> Unit,
    onClickDownloadQueue: () -> Unit,
    onClickCategories: () -> Unit,
    onClickStats: () -> Unit,
    onClickDataAndStorage: () -> Unit,
    onClickSettings: () -> Unit,
    onClickAbout: () -> Unit,
    extensionUpdatesCount: Int,
    onClickAddSources: () -> Unit,
    onClickMySources: () -> Unit,
    onClickExtensions: () -> Unit,
    onClickMigrate: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Scaffold { contentPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 20.dp,
                bottom = contentPadding.calculateBottomPadding() + 32.dp,
            ),
        ) {
            item(key = "header") {
                Text(
                    text = stringResource(MR.strings.nav_you),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Sources
            item(key = "sources_label") { SectionLabel(stringResource(MR.strings.you_section_sources)) }
            item(key = "add_hero") { AddSourcesHero(onClick = onClickAddSources) }
            item(key = "sources_cards") {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column {
                        SourceRow(
                            icon = MaterialSymbols.Rounded.Public,
                            title = stringResource(MR.strings.you_my_sources),
                            description = stringResource(MR.strings.you_my_sources_desc),
                            onClick = onClickMySources,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SourceRow(
                            icon = MaterialSymbols.Rounded.Code,
                            title = stringResource(MR.strings.you_extensions),
                            description = stringResource(MR.strings.you_extensions_desc),
                            badge = extensionUpdatesCount,
                            onClick = onClickExtensions,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SourceRow(
                            icon = MaterialSymbols.Rounded.SwapCalls,
                            title = stringResource(MR.strings.you_migrate),
                            description = stringResource(MR.strings.you_migrate_desc),
                            onClick = onClickMigrate,
                        )
                    }
                }
            }

            // Library & downloads
            item(key = "library_label") { SectionLabel(stringResource(MR.strings.you_section_library)) }
            item(key = "downloaded_only") {
                SwitchPreferenceWidget(
                    title = stringResource(MR.strings.label_downloaded_only),
                    subtitle = stringResource(MR.strings.downloaded_only_summary),
                    icon = MaterialSymbols.Rounded.CloudOff,
                    checked = downloadedOnly,
                    onCheckedChanged = onDownloadedOnlyChange,
                )
            }
            item(key = "incognito") {
                SwitchPreferenceWidget(
                    title = stringResource(MR.strings.pref_incognito_mode),
                    subtitle = stringResource(MR.strings.pref_incognito_mode_summary),
                    icon = MaterialSymbols.Rounded.Eyeglasses2,
                    checked = incognitoMode,
                    onCheckedChanged = onIncognitoModeChange,
                )
            }
            item(key = "download_queue") {
                val downloadQueueState = downloadQueueStateProvider()
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_download_queue),
                    subtitle = when (downloadQueueState) {
                        DownloadQueueState.Stopped -> null
                        is DownloadQueueState.Paused -> {
                            val pending = downloadQueueState.pending
                            if (pending == 0) {
                                stringResource(MR.strings.paused)
                            } else {
                                "${stringResource(MR.strings.paused)} • ${
                                    pluralStringResource(
                                        MR.plurals.download_queue_summary,
                                        count = pending,
                                        pending,
                                    )
                                }"
                            }
                        }
                        is DownloadQueueState.Downloading -> {
                            val pending = downloadQueueState.pending
                            pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                        }
                    },
                    icon = MaterialSymbols.Rounded.Download,
                    onPreferenceClick = onClickDownloadQueue,
                )
            }
            item(key = "categories") {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.categories),
                    icon = MaterialSymbols.AutoMirroredRounded.Label,
                    onPreferenceClick = onClickCategories,
                )
            }
            item(key = "data_storage") {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_data_storage),
                    icon = MaterialSymbols.Rounded.Storage,
                    onPreferenceClick = onClickDataAndStorage,
                )
            }
            item(key = "stats") {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_stats),
                    icon = MaterialSymbols.Rounded.QueryStats,
                    onPreferenceClick = onClickStats,
                )
            }

            // App
            item(key = "app_label") { SectionLabel(stringResource(MR.strings.you_section_app)) }
            item(key = "settings") {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_settings),
                    icon = MaterialSymbols.Rounded.Settings,
                    onPreferenceClick = onClickSettings,
                )
            }
            item(key = "about") {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.pref_category_about),
                    icon = MaterialSymbols.Rounded.Info,
                    onPreferenceClick = onClickAbout,
                )
            }
            item(key = "help") {
                TextPreferenceWidget(
                    title = stringResource(MR.strings.label_help),
                    icon = MaterialSymbols.AutoMirroredRounded.Help,
                    onPreferenceClick = { uriHandler.openUri(Constants.URL_HELP) },
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 8.dp),
    )
}

@Composable
private fun AddSourcesHero(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(MR.strings.you_add_hero_title),
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(MR.strings.you_add_hero_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onClick, shape = MaterialTheme.shapes.small) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(MR.strings.you_add_hero_button),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SourceRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    badge: Int = 0,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (badge > 0) {
            Box(
                modifier = Modifier
                    .height(22.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badge.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Icon(
            imageVector = MaterialSymbols.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
