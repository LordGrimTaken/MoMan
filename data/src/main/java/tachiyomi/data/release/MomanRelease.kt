package tachiyomi.data.release

import kotlinx.serialization.Serializable

/**
 * Latest release info served by the MoMan update server (`/latest.json`).
 * [apks] maps an ABI name (arm64-v8a, armeabi-v7a, x86_64, x86) or "universal" to a download URL.
 */
@Serializable
data class MomanRelease(
    val version: String,
    val versionCode: Int = 0,
    val minSupportedVersionCode: Int = 0,
    val changelog: String = "",
    val apks: Map<String, String> = emptyMap(),
)
