package pl.marollink.app.data

import org.json.JSONArray
import org.json.JSONObject

enum class WirelessTransportMode(val labelPl: String) {
    LOCAL_HOTSPOT("Lokalny hotspot"),
    WIFI_DIRECT_5GHZ("Wi-Fi Direct 5 GHz")
}

enum class VideoResolutionPreset(val label: String, val width: Int, val height: Int) {
    AUTO("Auto", 0, 0),
    HD_720("1280x720 (HD)", 1280, 720),
    FHD_1080("1920x1080 (Full HD)", 1920, 1080)
}

enum class MicSource(val labelPl: String) {
    CAR_HEAD_UNIT("Mikrofon centrali auta"),
    PHONE("Mikrofon telefonu")
}

enum class PanelPriority(val labelPl: String) {
    MAP("Mapa"),
    MEDIA("Multimedia")
}

enum class AppLanguage(val code: String, val displayName: String) {
    AUTO("auto", "Automatycznie (system)"),
    PL("pl", "Polski"),
    EN("en", "English"),
    PT("pt", "Português"),
    ES("es", "Español"),
    DE("de", "Deutsch"),
    FR("fr", "Français"),
    IT("it", "Italiano"),
    ZH("zh", "中文"),
    TR("tr", "Türkçe")
}

enum class SteeringAction(val id: String, val labelPl: String) {
    NEXT_TRACK("next_track", "Następny utwór"),
    PREV_TRACK("prev_track", "Poprzedni utwór"),
    PLAY_PAUSE("play_pause", "Odtwórz / Wstrzymaj"),
    VOICE_ASSISTANT("voice_assistant", "Asystent głosowy"),
    SWITCH_PANEL_VIEW("switch_panel", "Przełącz widok panelu kierowcy")
}

enum class DeviceTransportType(val labelPl: String) {
    USB("Kabel USB"),
    BLUETOOTH("Bezprzewodowo (BT/Wi-Fi)"),
    AUTOKIT_DONGLE("Adapter USB AutoKit")
}

enum class ProjectionProtocol(val labelPl: String) {
    UNKNOWN("Nieznany / Auto"),
    ANDROID_AUTO("Android Auto"),
    CARPLAY("Apple CarPlay")
}

data class KnownDevice(
    val id: String,
    val name: String,
    val addressOrSerial: String,
    val transportType: DeviceTransportType,
    val protocol: ProjectionProtocol,
    val lastConnectedEpochMs: Long
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("addressOrSerial", addressOrSerial)
        put("transportType", transportType.name)
        put("protocol", protocol.name)
        put("lastConnectedEpochMs", lastConnectedEpochMs)
    }

    companion object {
        fun fromJson(obj: JSONObject): KnownDevice? {
            val id = obj.optString("id", "")
            val name = obj.optString("name", "")
            if (id.isBlank() || name.isBlank()) return null
            val transport = runCatching {
                DeviceTransportType.valueOf(obj.optString("transportType", DeviceTransportType.BLUETOOTH.name))
            }.getOrDefault(DeviceTransportType.BLUETOOTH)
            val proto = runCatching {
                ProjectionProtocol.valueOf(obj.optString("protocol", ProjectionProtocol.UNKNOWN.name))
            }.getOrDefault(ProjectionProtocol.UNKNOWN)
            return KnownDevice(
                id = id,
                name = name,
                addressOrSerial = obj.optString("addressOrSerial", ""),
                transportType = transport,
                protocol = proto,
                lastConnectedEpochMs = obj.optLong("lastConnectedEpochMs", 0L)
            )
        }

        fun serializeList(devices: List<KnownDevice>): String {
            val arr = JSONArray()
            devices.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }

        fun deserializeList(raw: String?): List<KnownDevice> {
            if (raw.isNullOrBlank()) return emptyList()
            return runCatching {
                val arr = JSONArray(raw)
                buildList {
                    for (i in 0 until arr.length()) {
                        arr.optJSONObject(i)?.let { obj ->
                            fromJson(obj)?.let { add(it) }
                        }
                    }
                }
            }.getOrDefault(emptyList())
        }
    }
}

data class AppSettings(
    val termsAccepted: Boolean = false,
    val onboardingTipsCompleted: Boolean = false,
    val autoConnectUsb: Boolean = true,
    val fullscreenMode: Boolean = true,
    val accentHue: Float = 200f,
    val appLanguage: AppLanguage = AppLanguage.PL,
    val uiSoundsEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val showFpsCounter: Boolean = false,
    val floatingControlsEnabled: Boolean = true,
    val safeAreaTopDp: Int = 0,
    val safeAreaBottomDp: Int = 0,
    val safeAreaLeftDp: Int = 0,
    val safeAreaRightDp: Int = 0,
    val volumeBoostDb: Int = 0,
    val steeringMappings: Map<SteeringAction, Int> = defaultSteeringMappings(),
    val callRecordingFolderUri: String? = null,
    val carPlayResolution: VideoResolutionPreset = VideoResolutionPreset.AUTO,
    val carPlayFps: Int = 60,
    val carPlayHevcEnabled: Boolean = false,
    val carPlayDpi: Int = 200,
    val carPlaySmoothVideo: Boolean = true,
    val carPlayAlacLossless: Boolean = true,
    val carPlayHighQualityNavVoice: Boolean = true,
    val carPlayMainBufferedAudio: Boolean = false,
    val carPlayAudioViaCarBt: Boolean = false,
    val carPlayDisconnectCarBtOnWireless: Boolean = false,
    val carPlayAudioBufferCableMs: Int = 80,
    val carPlayAudioBufferWifiMs: Int = 180,
    val carPlayWirelessMode: WirelessTransportMode = WirelessTransportMode.LOCAL_HOTSPOT,
    val aaResolution: VideoResolutionPreset = VideoResolutionPreset.FHD_1080,
    val aaAutoDpi: Boolean = true,
    val aaManualDpi: Int = 200,
    val aaFps: Int = 60,
    val aaSmoothVideo: Boolean = true,
    val aaMicSource: MicSource = MicSource.CAR_HEAD_UNIT,
    val aaWirelessMode: WirelessTransportMode = WirelessTransportMode.WIFI_DIRECT_5GHZ,
    val aaAudioBufferCableMs: Int = 80,
    val aaAudioBufferWifiMs: Int = 180,
    val autoKitAutoConnect: Boolean = true,
    val autoKitAudioViaCarBt: Boolean = false,
    val autoKitUsePhoneMic: Boolean = false,
    val autoKitFps: Int = 60,
    val autoKitPassthroughMode: Boolean = false,
    val panelEnabled: Boolean = false,
    val panelReceiveNavAndMedia: Boolean = true,
    val panelShowMinimap: Boolean = true,
    val panelPriority: PanelPriority = PanelPriority.MAP,
    val panelAnnounceTrackChange5s: Boolean = true,
    val panelShowStatsCard: Boolean = false,
    val panelAutoCycleSeconds: Int = 0,
    val panelCalibrationGridEnabled: Boolean = false,
    val sideCameraOnTurnSignal: Boolean = false,
    val dismissedUpdateTag: String? = null
) {
    companion object {
        fun defaultSteeringMappings(): Map<SteeringAction, Int> = mapOf(
            SteeringAction.NEXT_TRACK to android.view.KeyEvent.KEYCODE_MEDIA_NEXT,
            SteeringAction.PREV_TRACK to android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            SteeringAction.PLAY_PAUSE to android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            SteeringAction.VOICE_ASSISTANT to android.view.KeyEvent.KEYCODE_VOICE_ASSIST,
            SteeringAction.SWITCH_PANEL_VIEW to android.view.KeyEvent.KEYCODE_MENU
        )

        fun serializeSteeringMap(map: Map<SteeringAction, Int>): String {
            val obj = JSONObject()
            map.forEach { (action, code) -> obj.put(action.id, code) }
            return obj.toString()
        }

        fun deserializeSteeringMap(raw: String?): Map<SteeringAction, Int> {
            val defaults = defaultSteeringMappings().toMutableMap()
            if (raw.isNullOrBlank()) return defaults
            runCatching {
                val obj = JSONObject(raw)
                SteeringAction.entries.forEach { action ->
                    if (obj.has(action.id)) {
                        defaults[action] = obj.getInt(action.id)
                    }
                }
            }
            return defaults
        }
    }
}
