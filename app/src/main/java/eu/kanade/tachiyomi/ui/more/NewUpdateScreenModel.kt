package eu.kanade.tachiyomi.ui.more

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import com.moman.domain.base.BasePreferences
import com.moman.domain.release.model.ApkIdentity
import com.moman.domain.release.model.TransferProgress
import eu.kanade.tachiyomi.extension.util.ExtensionInstaller
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.network.ProgressListener
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.newCachelessCallWithProgress
import eu.kanade.tachiyomi.util.storage.getUriCompat
import eu.kanade.tachiyomi.util.storage.saveTo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import com.moman.core.common.util.system.logcat
import java.io.File
import java.security.MessageDigest

@AssistedInject
// The injected Context is the Application instance (App.kt builds the graph with `context = this`), so holding
// it for the ViewModel lifetime cannot leak an Activity.
@SuppressLint("StaticFieldLeak")
class NewUpdateScreenModel(
    @Assisted changelogInfo: String,
    @Assisted private val downloadLink: String,
    private val context: Context,
    private val network: NetworkHelper,
    private val preferences: BasePreferences,
) : ViewModel() {

    val state: StateFlow<NewUpdateScreenModel.State>
        field = MutableStateFlow<NewUpdateScreenModel.State>(State(changelogInfo = changelogInfo))

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(changelogInfo: String, downloadLink: String): NewUpdateScreenModel
    }

    private val apkFile: File
        get() = File(context.cacheDir, "update.apk")

    private var downloadJob: Job? = null

    fun startDownload() {
        if (downloadJob?.isActive == true) return

        downloadJob = viewModelScope.launch {
            state.update { it.copy(downloadProgress = 0, stage = Stage.Downloading) }
            try {
                apkFile.delete()
                withContext(Dispatchers.IO) {
                    downloadApk()
                    verifyDownloadedApk()
                }
                state.update { it.copy(downloadProgress = 100, stage = Stage.Downloaded) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e)
                apkFile.delete()
                state.update { it.copy(stage = Stage.Failed) }
            }
        }
    }

    private suspend fun downloadApk() {
        val progressListener = object : ProgressListener {
            // Progress of the download
            var savedProgress = 0

            // Keep track of the last update sent to avoid updating the state too often.
            var lastTick = 0L

            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                val progress = TransferProgress.percent(bytesRead, contentLength) ?: return
                val currentTime = System.currentTimeMillis()
                if (progress > savedProgress && currentTime - 200 > lastTick) {
                    savedProgress = progress
                    lastTick = currentTime
                    state.update { it.copy(downloadProgress = progress) }
                }
            }
        }

        val response = network.client.newCachelessCallWithProgress(GET(downloadLink), progressListener).awaitSuccess()
        response.body.source().saveTo(apkFile)
    }

    /**
     * Refuses a downloaded APK that is not this app: same package id and the same signing certificate as the
     * running build. Runs before the user is offered "Install", so a swapped file fails here instead of inside
     * Android's installer.
     */
    private fun verifyDownloadedApk() {
        val pm = context.packageManager
        val archive = pm.getPackageArchiveInfo(
            apkFile.absolutePath,
            PackageManager.GET_SIGNING_CERTIFICATES,
        ) ?: error("downloaded file is not a readable APK")
        val installed = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)

        val trusted = ApkIdentity.isTrustedUpdate(
            expectedPackage = context.packageName,
            actualPackage = archive.packageName,
            expectedCerts = certFingerprints(installed),
            actualCerts = certFingerprints(archive),
        )
        if (!trusted) {
            error("downloaded APK failed the identity check (package or signing certificate mismatch)")
        }
    }

    private fun certFingerprints(info: PackageInfo): Set<String> {
        // signingInfo is API 28+; on 26-27 the (deprecated) signatures field is the only source.
        val signatures: Array<Signature>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            info.signatures
        }
        return signatures.orEmpty()
            .map { signature ->
                MessageDigest.getInstance("SHA-256")
                    .digest(signature.toByteArray())
                    .joinToString("") { "%02x".format(it) }
            }
            .toSet()
    }

    fun skipUpdate(versionName: String) {
        downloadJob?.cancel()
        preferences.skippedAppUpdate.set(versionName)
    }

    fun installUpdate() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkFile.getUriCompat(context), ExtensionInstaller.APK_MIME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

    @Immutable
    data class State(
        val changelogInfo: String,
        val downloadProgress: Int = 0,
        val stage: Stage = Stage.Available,
    )

    enum class Stage {
        Available,
        Downloading,
        Downloaded,
        Failed,
    }
}
