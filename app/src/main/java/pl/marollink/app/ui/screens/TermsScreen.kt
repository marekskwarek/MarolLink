package pl.marollink.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.marollink.app.BuildConfig
import pl.marollink.app.ui.components.CarCard
import pl.marollink.app.ui.components.CarPrimaryButton
import pl.marollink.app.ui.theme.CarPalette

@Composable
fun TermsScreen(
    uiSoundsEnabled: Boolean,
    onAccept: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarPalette.Background)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        CarCard(
            modifier = Modifier
                .widthIn(max = 880.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Column {
                    Text(
                        text = "Warunki użytkowania i wyłączenie odpowiedzialności",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "MarolLink v${BuildConfig.VERSION_NAME} • Niezależna edycja lokalna",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BulletLine("MarolLink jest darmowym, niezależnym projektem niepowiązanym z firmami Leapmotor, Apple ani Google.")
                BulletLine("Aplikacja działa w 100% lokalnie w Twoim samochodzie: nie wymaga konta, kodów QR, numeru VIN ani zewnętrznych serwerów aktywacyjnych.")
                BulletLine("Ustawienia, sparowane telefony i dziennik diagnostyczny zapisywane są wyłącznie w pamięci tego urządzenia.")
                BulletLine("Oprogramowanie dostarczane jest w stanie „tak jak jest” (AS IS). Wszystkie zmiany ustawień i aktualizacje przeprowadzaj wyłącznie podczas postoju.")
                BulletLine("Kierowca ponosi pełną odpowiedzialność za bezpieczne prowadzenie pojazdu i przestrzeganie przepisów ruchu drogowego.")
            }

            Spacer(modifier = Modifier.size(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                CarPrimaryButton(
                    text = "Kontynuuj",
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = onAccept,
                    uiSoundsEnabled = uiSoundsEnabled,
                    modifier = Modifier.widthIn(min = 240.dp)
                )
            }
        }
    }
}

@Composable
private fun BulletLine(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = CarPalette.TextPrimary
        )
    }
}
