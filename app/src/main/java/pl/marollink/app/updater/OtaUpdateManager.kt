package pl.marollink.app.updater

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.marollink.app.BuildConfig
import pl.marollink.app.data.DiagnosticLogManager
import java.io.File

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data class UpToDate(val currentVersion: String) : UpdateUiState
    data class UpdateAvailable(val release: VerifiedGitHubRelease) : UpdateUiState
    data class Downloading(
        val release: VerifiedGitHubRelease,
        val percent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : UpdateUiState

    data class Verifying(val release: VerifiedGitHubRelease) : UpdateUiState
    data class NeedsInstallPermission(val release: VerifiedGitHubRelease, val apkFile: File) : UpdateUiState
    data class ReadyToInstall(val release: VerifiedGitHubRelease, val apkFile: File) : UpdateUiState
    data class Error(val messagePl: String, val lastRelease: VerifiedGitHubRelease? = null) : UpdateUiState
}

class OtaUpdateManager(
    private val context: Context,
    private val logManager: DiagnosticLogManager,
    private val scope: CoroutineScope
) {
    private val releaseClient = GitHubReleaseClient(
        owner = BuildConfig.GITHUB_OWNER,
        repo = BuildConfig.GITHUB_REPO,
        userAgent = "MarolLink-Android/${BuildConfig.VERSION_NAME}"
    )
    private val downloader = ApkDownloader(context)
    private val installer = ApkInstaller(context)

    private val _state = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()

    private val _latestAvailableRelease = MutableStateFlow<VerifiedGitHubRelease?>(null)
    val latestAvailableRelease: StateFlow<VerifiedGitHubRelease?> = _latestAvailableRelease.asStateFlow()

    private var activeJob: Job? = null
    private var startupChecked = false

    fun checkOnStartupIfOnline(dismissedTag: String?) {
        if (startupChecked) return
        startupChecked = true
        if (!isNetworkAvailable()) {
            logManager.log("INFO", "OTA", "Brak połączenia z internetem przy starcie — pominięto ciche sprawdzanie OTA")
            return
        }
        scope.launch {
            val currentSemVer = SemanticVersion.parse(BuildConfig.VERSION_NAME) ?: SemanticVersion(1, 0, 0)
            when (val result = releaseClient.fetchLatestRelease()) {
                is ReleaseFetchResult.Success -> {
                    val release = result.release
                    if (release.version > currentSemVer) {
                        _latestAvailableRelease.value = release
                        logManager.log("INFO", "OTA", "Wykryto nowszą wersję na GitHub Releases: ${release.tagName}")
                        if (dismissedTag != release.tagName) {
                            _state.value = UpdateUiState.UpdateAvailable(release)
                        }
                    } else {
                        logManager.log("INFO", "OTA", "Zainstalowana wersja (${BuildConfig.VERSION_NAME}) jest aktualna")
                    }
                }
                ReleaseFetchResult.NoReleasesPublished -> {
                    logManager.log("INFO", "OTA", "Brak wydań w repozytorium GitHub Releases")
                }
                is ReleaseFetchResult.Failure -> {
                    // Fail silently on startup so offline/slow network never interrupts the driver
                    logManager.log("WARN", "OTA", "Ciche sprawdzanie OTA przerwane: ${result.reasonPl}")
                }
            }
        }
    }

    fun checkManually() {
        if (activeJob?.isActive == true) return
        if (!isNetworkAvailable()) {
            _state.value = UpdateUiState.Error("Brak połączenia z internetem. Podłącz radio do Wi-Fi lub hotspotu telefonu.")
            return
        }
        activeJob = scope.launch {
            _state.value = UpdateUiState.Checking
            val currentSemVer = SemanticVersion.parse(BuildConfig.VERSION_NAME) ?: SemanticVersion(1, 0, 0)
            when (val result = releaseClient.fetchLatestRelease()) {
                is ReleaseFetchResult.Success -> {
                    val release = result.release
                    if (release.version > currentSemVer) {
                        _latestAvailableRelease.value = release
                        _state.value = UpdateUiState.UpdateAvailable(release)
                    } else {
                        _latestAvailableRelease.value = null
                        _state.value = UpdateUiState.UpToDate(BuildConfig.VERSION_NAME)
                    }
                }
                ReleaseFetchResult.NoReleasesPublished -> {
                    _state.value = UpdateUiState.UpToDate(BuildConfig.VERSION_NAME)
                    logManager.log("INFO", "OTA", "Repozytorium nie ma jeszcze opublikowanych wydań")
                }
                is ReleaseFetchResult.Failure -> {
                    _state.value = UpdateUiState.Error(result.reasonPl)
                    logManager.log("WARN", "OTA", "Ręczne sprawdzanie OTA nie powiodło się: ${result.reasonPl}")
                }
            }
        }
    }

    fun startDownloadAndInstall(release: VerifiedGitHubRelease) {
        if (activeJob?.isActive == true) return
        activeJob = scope.launch {
            _state.value = UpdateUiState.Downloading(release, 0, 0L, release.apkSizeBytes)
            logManager.log("INFO", "OTA", "Rozpoczęto pobieranie ${release.apkFileName}")
            val result = downloader.downloadAndVerify(
                release = release,
                onProgress = { percent, down, total ->
                    _state.value = UpdateUiState.Downloading(release, percent, down, total)
                },
                onVerifying = {
                    _state.value = UpdateUiState.Verifying(release)
                }
            )
            when (result) {
                is DownloadResult.Success -> {
                    logManager.log("INFO", "OTA", "SHA-256 zweryfikowano poprawnie dla ${release.apkFileName}")
                    triggerInstallOrRequestPermission(release, result.apkFile)
                }
                is DownloadResult.Failure -> {
                    logManager.log("ERROR", "OTA", "Błąd aktualizacji: ${result.reasonPl}")
                    _state.value = UpdateUiState.Error(result.reasonPl, lastRelease = release)
                }
            }
        }
    }

    fun openPermissionSettings() {
        installer.openInstallPermissionSettings()
    }

    fun resumePendingInstallIfPermitted() {
        val current = _state.value
        if (current is UpdateUiState.NeedsInstallPermission && installer.canInstallPackages()) {
            triggerInstallOrRequestPermission(current.release, current.apkFile)
        }
    }

    fun launchPendingInstall() {
        val current = _state.value
        val (release, file) = when (current) {
            is UpdateUiState.ReadyToInstall -> current.release to current.apkFile
            is UpdateUiState.NeedsInstallPermission -> current.release to current.apkFile
            else -> return
        }
        triggerInstallOrRequestPermission(release, file)
    }

    fun dismissDialog() {
        if (_state.value !is UpdateUiState.Downloading && _state.value !is UpdateUiState.Verifying) {
            _state.value = UpdateUiState.Idle
        }
    }

    fun showAvailableDialogIfAny() {
        val available = _latestAvailableRelease.value
        if (available != null) {
            _state.value = UpdateUiState.UpdateAvailable(available)
        } else {
            checkManually()
        }
    }

    private fun triggerInstallOrRequestPermission(release: VerifiedGitHubRelease, apkFile: File) {
        if (!installer.canInstallPackages()) {
            _state.value = UpdateUiState.NeedsInstallPermission(release, apkFile)
            logManager.log("INFO", "OTA", "Wymagane uprawnienie REQUEST_INSTALL_PACKAGES przed otwarciem instalatora")
            return
        }
        val launched = installer.launchInstaller(apkFile)
        if (launched) {
            _state.value = UpdateUiState.ReadyToInstall(release, apkFile)
            logManager.log("INFO", "OTA", "Otwarto systemowy instalator Androida dla ${release.tagName}")
        } else {
            _state.value = UpdateUiState.Error(
                "System nie pozwolił otworzyć instalatora pakietów dla pobranego pliku APK.",
                lastRelease = release
            )
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
