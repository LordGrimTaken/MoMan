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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
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
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.R
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
            item(key = "modes") {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column {
                        ModeRow(
                            icon = MaterialSymbols.Rounded.CloudOff,
                            title = stringResource(MR.strings.label_downloaded_only),
                            subtitle = stringResource(MR.strings.downloaded_only_summary),
                            checked = downloadedOnly,
                            onCheckedChange = onDownloadedOnlyChange,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        ModeRow(
                            icon = MaterialSymbols.Rounded.Eyeglasses2,
                            title = stringResource(MR.strings.pref_incognito_mode),
                            subtitle = stringResource(MR.strings.pref_incognito_mode_summary),
                            checked = incognitoMode,
                            onCheckedChange = onIncognitoModeChange,
                        )
                    }
                }
            }
            item(key = "tiles") {
                val downloadQueueState = downloadQueueStateProvider()
                val queueSubtitle: String? = when (downloadQueueState) {
                    DownloadQueueState.Stopped -> null
                    is DownloadQueueState.Paused -> {
                        val pending = downloadQueueState.pending
                        if (pending == 0) {
                            stringResource(MR.strings.paused)
                        } else {
                            "${stringResource(MR.strings.paused)} • ${
                                pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                            }"
                        }
                    }
                    is DownloadQueueState.Downloading -> {
                        val pending = downloadQueueState.pending
                        pluralStringResource(MR.plurals.download_queue_summary, count = pending, pending)
                    }
                }
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LibraryTile(
                            icon = MaterialSymbols.Rounded.Download,
                            title = stringResource(MR.strings.label_download_queue),
                            subtitle = queueSubtitle,
                            onClick = onClickDownloadQueue,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                        LibraryTile(
                            icon = MaterialSymbols.AutoMirroredRounded.Label,
                            title = stringResource(MR.strings.categories),
                            subtitle = null,
                            onClick = onClickCategories,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LibraryTile(
                            icon = MaterialSymbols.Rounded.Storage,
                            title = stringResource(MR.strings.label_data_storage),
                            subtitle = null,
                            onClick = onClickDataAndStorage,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                        LibraryTile(
                            icon = MaterialSymbols.Rounded.QueryStats,
                            title = stringResource(MR.strings.label_stats),
                            subtitle = null,
                            onClick = onClickStats,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }

            // App
            item(key = "app_label") { SectionLabel(stringResource(MR.strings.you_section_app)) }
            item(key = "app_card") {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column {
                        AppRow(
                            icon = MaterialSymbols.Rounded.Settings,
                            title = stringResource(MR.strings.label_settings),
                            description = stringResource(MR.strings.you_settings_desc),
                            onClick = onClickSettings,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        AppRow(
                            icon = MaterialSymbols.Rounded.Info,
                            title = stringResource(MR.strings.pref_category_about),
                            description = stringResource(MR.strings.you_about_desc),
                            onClick = onClickAbout,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        AppRow(
                            icon = MaterialSymbols.AutoMirroredRounded.Help,
                            title = stringResource(MR.strings.label_help),
                            description = stringResource(MR.strings.you_help_desc),
                            onClick = { uriHandler.openUri(Constants.URL_HELP) },
                        )
                    }
                }
            }
            item(key = "footer") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_moman),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(44.dp),
                    )
                    Text(
                        text = "\u58A8\u6F2B MoMan",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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


@Composable
private fun IconTile(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ModeRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconTile(icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun LibraryTile(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconTile(icon)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconTile(icon)
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
        Icon(
            imageVector = MaterialSymbols.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
