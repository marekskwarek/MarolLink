package pl.marollink.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.marollink.app.ui.components.CarCard
import pl.marollink.app.ui.components.CarPrimaryButton
import pl.marollink.app.ui.components.CarSecondaryButton
import pl.marollink.app.ui.components.StatusChip
import pl.marollink.app.ui.theme.CarPalette

@Composable
fun OnboardingTipsScreen(
    uiSoundsEnabled: Boolean,
    onFinish: () -> Unit
) {
    var step by rememberSaveable { mutableIntStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarPalette.Background)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        CarCard(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = if (step == 1) Icons.Rounded.Bluetooth else Icons.Rounded.Wifi,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(38.dp)
                    )
                    Text(
                        text = if (step == 1) {
                            "Usuń telefon z Bluetooth samochodu"
                        } else {
                            "Wyłącz Wi-Fi systemu multimedialnego"
                        },
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                StatusChip(
                    text = "Wskazówka $step z 2",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (step == 1) {
                    "Aby połączenie bezprzewodowe działało stabilnie, usuń wcześniejsze parowanie swojego telefonu z wbudowanego zestawu Bluetooth samochodu (oraz zapomnij samochód w ustawieniach Bluetooth telefonu).\n\nJeśli auto i MarolLink próbują jednocześnie przejąć ten sam telefon przez Bluetooth, połączenie może się zrywać."
                } else {
                    "Jeśli używasz trybu lokalnego hotspotu 2,4 GHz, wyłącz zwykłe Wi-Fi w ustawieniach centrali samochodu — w przeciwnym razie radio ciągle skanuje sieci i powoduje przycinanie dźwięku oraz obrazu.\n\nWyjątek: dla trybu Wi-Fi Direct 5 GHz (Android Auto) moduł Wi-Fi centrali musi pozostać włączony."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = CarPalette.TextPrimary
            )

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step == 2) {
                    CarSecondaryButton(
                        text = "Wstecz",
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        onClick = { step = 1 },
                        uiSoundsEnabled = uiSoundsEnabled
                    )
                } else {
                    Spacer(modifier = Modifier.size(1.dp))
                }

                if (step == 1) {
                    CarPrimaryButton(
                        text = "Dalej",
                        icon = Icons.AutoMirrored.Rounded.ArrowForward,
                        onClick = { step = 2 },
                        uiSoundsEnabled = uiSoundsEnabled,
                        modifier = Modifier.widthIn(min = 220.dp)
                    )
                } else {
                    CarPrimaryButton(
                        text = "Zacznij korzystać z MarolLink",
                        icon = Icons.Rounded.CheckCircle,
                        onClick = onFinish,
                        uiSoundsEnabled = uiSoundsEnabled
                    )
                }
            }
        }
    }
}
