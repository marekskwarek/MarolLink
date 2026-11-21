package pl.marollink.app.ui.theme

import android.view.SoundEffectConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Composable
fun rememberCarClickFeedback(enabled: Boolean): () -> Unit {
    val view = LocalView.current
    return remember(view, enabled) {
        {
            if (enabled) {
                view.playSoundEffect(SoundEffectConstants.CLICK)
            }
        }
    }
}
