package pl.marollink.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pl.marollink.app.BuildConfig
import pl.marollink.app.ui.theme.CarPalette
import pl.marollink.app.updater.UpdateUiState
import pl.marollink.app.updater.VerifiedGitHubRelease

@Composable
fun OtaUpdateDialog(
    state: UpdateUiState,
    uiSoundsEnabled: Boolean,
    onStartUpdate: (VerifiedGitHubRelease) -> Unit,
    onDismiss: (dismissedTag: String?) -> Unit,
    onOpenInstallPermissions: () -> Unit,
    onRelaunchInstaller: () -> Unit,
    onRetry: (VerifiedGitHubRelease?) -> Unit
) {
    if (state is UpdateUiState.Idle) return

    val canDismissOutside = state !is UpdateUiState.Downloading && state !is UpdateUiState.Verifying

    Dialog(
        onDismissRequest = {
            if (canDismissOutside) {
                onDismiss((state as? UpdateUiState.UpdateAvailable)?.release?.tagName)
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = canDismissOutside,
            dismissOnClickOutside = canDismissOutside,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = 480.dp, max = 680.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(22.dp),
            color = CarPalette.Surface,
            border = BorderStroke(1.5.dp, CarPalette.Border)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (state) {
                    UpdateUiState.Checking -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(34.dp))
                            Column {
                                Text(
                                    text = "Sprawdzanie aktualizacji",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Łączę z GitHub Releases (${BuildConfig.GITHUB_OWNER}/${BuildConfig.GITHUB_REPO})…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CarPalette.TextSecondary
                                )
                            }
                        }
                    }

                    is UpdateUiState.UpToDate -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = CarPalette.Success,
                                modifier = Modifier.size(36.dp)
                            )
                            Column {
                                Text(
                                    text = "Aplikacja jest aktualna",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Zainstalowana wersja: ${state.currentVersion}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CarPalette.TextSecondary
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            CarPrimaryButton(
                                text = "Zamknij",
                                onClick = { onDismiss(null) },
                                uiSoundsEnabled = uiSoundsEnabled
                            )
                        }
                    }

                    is UpdateUiState.UpdateAvailable -> {
                        val rel = state.release
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SystemUpdate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(34.dp)
                                )
                                Column {
                                    Text(
                                        text = "Dostępna nowa wersja MarolLink",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Text(
                                        text = "Obecna: ${BuildConfig.VERSION_NAME}  →  Nowa: ${rel.tagName}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            StatusChip(text = rel.tagName, color = MaterialTheme.colorScheme.primary)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp, max = 180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CarPalette.Background)
                                .padding(14.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = rel.releaseNotes,
                                style = MaterialTheme.typography.bodyLarge,
                                color = CarPalette.TextPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                        ) {
                            CarSecondaryButton(
                                text = "Nie teraz",
                                onClick = { onDismiss(rel.tagName) },
                                uiSoundsEnabled = uiSoundsEnabled
                            )
                            CarPrimaryButton(
                                text = "Aktualizuj teraz",
                                icon = Icons.Rounded.SystemUpdate,
                                onClick = { onStartUpdate(rel) },
                                uiSoundsEnabled = uiSoundsEnabled,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    is UpdateUiState.Downloading -> {
                        Text(
                            text = "Pobieranie wersji ${state.release.tagName}…",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        LinearProgressIndicator(
                            progress = { state.percent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = CarPalette.SurfaceElevated
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${formatMb(state.downloadedBytes)} / ${formatMb(state.totalBytes)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CarPalette.TextSecondary
                            )
                            Text(
                                text = "${state.percent}%",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    is UpdateUiState.Verifying -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(34.dp))
                            Column {
                                Text(
                                    text = "Weryfikacja bezpieczeństwa pliku",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Sprawdzam sumę kontrolną SHA-256 pobranego pliku APK…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CarPalette.TextSecondary
                                )
                            }
                        }
                    }

                    is UpdateUiState.NeedsInstallPermission -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = CarPalette.Warning,
                                modifier = Modifier.size(34.dp)
                            )
                            Column {
                                Text(
                                    text = "Wymagana zgoda na instalację",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Aktualizacja została pobrana i sprawdzona (SHA-256 OK). Zezwól systemowi Android na instalowanie aktualizacji przez MarolLink, a potem wróć tutaj.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CarPalette.TextSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                        ) {
                            CarSecondaryButton(
                                text = "Później",
                                onClick = { onDismiss(null) },
                                uiSoundsEnabled = uiSoundsEnabled
                            )
                            CarPrimaryButton(
                                text = "Otwórz ustawienia zgody",
                                onClick = onOpenInstallPermissions,
                                uiSoundsEnabled = uiSoundsEnabled,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    is UpdateUiState.ReadyToInstall -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = CarPalette.Success,
                                modifier = Modifier.size(34.dp)
                            )
                            Column {
                                Text(
                                    text = "Instalator gotowy (${state.release.tagName})",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Suma SHA-256 poprawna. Zatwierdź instalację w systemowym oknie Androida.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CarPalette.TextSecondary
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                        ) {
                            CarSecondaryButton(
                                text = "Zamknij",
                                onClick = { onDismiss(null) },
                                uiSoundsEnabled = uiSoundsEnabled
                            )
                            CarPrimaryButton(
                                text = "Otwórz instalator ponownie",
                                onClick = onRelaunchInstaller,
                                uiSoundsEnabled = uiSoundsEnabled
                            )
                        }
                    }

                    is UpdateUiState.Error -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = CarPalette.Danger,
                                modifier = Modifier.size(34.dp)
                            )
                            Column {
                                Text(
                                    text = "Problem z aktualizacją",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = state.messagePl,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CarPalette.TextSecondary
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                        ) {
                            CarSecondaryButton(
                                text = "Zamknij",
                                onClick = { onDismiss(null) },
                                uiSoundsEnabled = uiSoundsEnabled
                            )
                            CarPrimaryButton(
                                text = "Spróbuj ponownie",
                                onClick = { onRetry(state.lastRelease) },
                                uiSoundsEnabled = uiSoundsEnabled
                            )
                        }
                    }

                    UpdateUiState.Idle -> Unit
                }
            }
        }
    }
}

private fun formatMb(bytes: Long): String {
    if (bytes <= 0L) return "0.0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    return String.format(java.util.Locale.US, "%.1f MB", mb)
}
