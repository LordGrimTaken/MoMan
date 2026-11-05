package eu.kanade.presentation.category

import android.content.Context
import androidx.compose.runtime.Composable
import com.moman.core.common.i18n.stringResource
import com.moman.domain.category.model.Category
import com.moman.i18n.MR
import com.moman.presentation.core.i18n.stringResource

val Category.visualName: String
    @Composable
    get() = when {
        isSystemCategory -> stringResource(MR.strings.label_default)
        else -> name
    }

fun Category.visualName(context: Context): String =
    when {
        isSystemCategory -> context.stringResource(MR.strings.label_default)
        else -> name
    }
