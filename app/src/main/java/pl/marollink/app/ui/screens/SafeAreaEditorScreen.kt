package pl.marollink.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.marollink.app.data.AppSettings
import pl.marollink.app.ui.components.CalibrationGridOverlay
import pl.marollink.app.ui.components.CarCard
import pl.marollink.app.ui.components.CarPrimaryButton
import pl.marollink.app.ui.components.CarSecondaryButton
import pl.marollink.app.ui.components.CarSliderRow
import pl.marollink.app.ui.components.CarSwitchRow
import pl.marollink.app.ui.theme.CarPalette
import kotlin.math.roundToInt

@Composable
fun SafeAreaEditorScreen(
    settings: AppSettings,
    onUpdateMargins: (top: Int, bottom: Int, left: Int, right: Int) -> Unit,
    onToggleGrid: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarPalette.Background)
    ) {
        // Simulated safe area bounding box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = settings.safeAreaTopDp.dp,
                    bottom = settings.safeAreaBottomDp.dp,
                    start = settings.safeAreaLeftDp.dp,
                    end = settings.safeAreaRightDp.dp
                )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = CarPalette.Background,
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    CarCard(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Kalibracja bezpiecznego obszaru ekranu",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "Dostosuj odstępy od krawędzi, aby paski systemu auta nie zasłaniały projekcji.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CarPalette.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            CarSliderRow(
                                title = "Margines górny",
                                valueLabel = "${settings.safeAreaTopDp} dp",
                                value = settings.safeAreaTopDp.toFloat(),
                                valueRange = 0f..120f,
                                onValueChange = {
                                    onUpdateMargins(
                                        it.roundToInt(),
                                        settings.safeAreaBottomDp,
                                        settings.safeAreaLeftDp,
                                        settings.safeAreaRightDp
                                    )
                                }
                            )
                            CarSliderRow(
                                title = "Margines dolny",
                                valueLabel = "${settings.safeAreaBottomDp} dp",
                                value = settings.safeAreaBottomDp.toFloat(),
                                valueRange = 0f..120f,
                                onValueChange = {
                                    onUpdateMargins(
                                        settings.safeAreaTopDp,
                                        it.roundToInt(),
                                        settings.safeAreaLeftDp,
                                        settings.safeAreaRightDp
                                    )
                                }
                            )
                            CarSliderRow(
                                title = "Margines lewy",
                                valueLabel = "${settings.safeAreaLeftDp} dp",
                                value = settings.safeAreaLeftDp.toFloat(),
                                valueRange = 0f..160f,
                                onValueChange = {
                                    onUpdateMargins(
                                        settings.safeAreaTopDp,
                                        settings.safeAreaBottomDp,
                                        it.roundToInt(),
                                        settings.safeAreaRightDp
                                    )
                                }
                            )
                            CarSliderRow(
                                title = "Margines prawy",
                                valueLabel = "${settings.safeAreaRightDp} dp",
                                value = settings.safeAreaRightDp.toFloat(),
                                valueRange = 0f..160f,
                                onValueChange = {
                                    onUpdateMargins(
                                        settings.safeAreaTopDp,
                                        settings.safeAreaBottomDp,
                                        settings.safeAreaLeftDp,
                                        it.roundToInt()
                                    )
                                }
                            )

                            CarSwitchRow(
                                title = "Wzorzec testowy (siatka co 20 dp)",
                                subtitle = "Wyświetla podziałkę na ekranie do oceny widocznego obszaru.",
                                checked = settings.panelCalibrationGridEnabled,
                                onCheckedChange = onToggleGrid,
                                uiSoundsEnabled = settings.uiSoundsEnabled
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.75f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        CarCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Rounded.AspectRatio,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text("Podgląd ramki", style = MaterialTheme.typography.titleLarge)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Kolorowa obwódka na zewnątrz wyznacza dokładny obszar roboczy CarPlay / Android Auto.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CarPalette.TextSecondary
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CarSecondaryButton(
                                text = "Zeruj marginesy",
                                icon = Icons.Rounded.Refresh,
                                onClick = { onUpdateMargins(0, 0, 0, 0) },
                                uiSoundsEnabled = settings.uiSoundsEnabled,
                                modifier = Modifier.fillMaxWidth()
                            )
                            CarPrimaryButton(
                                text = "Zapisz i wróć",
                                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                                onClick = onBack,
                                uiSoundsEnabled = settings.uiSoundsEnabled,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        if (settings.panelCalibrationGridEnabled) {
            CalibrationGridOverlay()
        }
    }
}
