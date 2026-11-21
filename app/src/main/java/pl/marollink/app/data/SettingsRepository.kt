package pl.marollink.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.marolLinkDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "marollink_settings"
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_tips_done")
        val AUTO_CONNECT_USB = booleanPreferencesKey("auto_connect_usb")
        val FULLSCREEN_MODE = booleanPreferencesKey("fullscreen_mode")
        val ACCENT_HUE = floatPreferencesKey("accent_hue")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val UI_SOUNDS = booleanPreferencesKey("ui_sounds_enabled")
        val ANIMATIONS = booleanPreferencesKey("animations_enabled")
        val SHOW_FPS = booleanPreferencesKey("show_fps_counter")
        val FLOATING_CONTROLS = booleanPreferencesKey("floating_controls")
        val SAFE_TOP = intPreferencesKey("safe_area_top_dp")
        val SAFE_BOTTOM = intPreferencesKey("safe_area_bottom_dp")
        val SAFE_LEFT = intPreferencesKey("safe_area_left_dp")
        val SAFE_RIGHT = intPreferencesKey("safe_area_right_dp")
        val VOLUME_BOOST_DB = intPreferencesKey("volume_boost_db")
        val STEERING_MAP_JSON = stringPreferencesKey("steering_map_json")
        val CALL_REC_FOLDER_URI = stringPreferencesKey("call_recording_folder_uri")

        val CP_RESOLUTION = stringPreferencesKey("cp_resolution")
        val CP_FPS = intPreferencesKey("cp_fps")
        val CP_HEVC = booleanPreferencesKey("cp_hevc")
        val CP_DPI = intPreferencesKey("cp_dpi")
        val CP_SMOOTH = booleanPreferencesKey("cp_smooth_video")
        val CP_ALAC = booleanPreferencesKey("cp_alac")
        val CP_HQ_NAV = booleanPreferencesKey("cp_hq_nav")
        val CP_MAIN_BUF = booleanPreferencesKey("cp_main_buffered_audio")
        val CP_AUDIO_BT = booleanPreferencesKey("cp_audio_car_bt")
        val CP_DISCONNECT_BT = booleanPreferencesKey("cp_disconnect_bt_wireless")
        val CP_BUF_CABLE = intPreferencesKey("cp_buf_cable_ms")
        val CP_BUF_WIFI = intPreferencesKey("cp_buf_wifi_ms")
        val CP_WIRELESS_MODE = stringPreferencesKey("cp_wireless_mode")

        val AA_RESOLUTION = stringPreferencesKey("aa_resolution")
        val AA_AUTO_DPI = booleanPreferencesKey("aa_auto_dpi")
        val AA_MANUAL_DPI = intPreferencesKey("aa_manual_dpi")
        val AA_FPS = intPreferencesKey("aa_fps")
        val AA_SMOOTH = booleanPreferencesKey("aa_smooth_video")
        val AA_MIC_SOURCE = stringPreferencesKey("aa_mic_source")
        val AA_WIRELESS_MODE = stringPreferencesKey("aa_wireless_mode")
        val AA_BUF_CABLE = intPreferencesKey("aa_buf_cable_ms")
        val AA_BUF_WIFI = intPreferencesKey("aa_buf_wifi_ms")

        val AK_AUTO_CONNECT = booleanPreferencesKey("ak_auto_connect")
        val AK_AUDIO_BT = booleanPreferencesKey("ak_audio_bt")
        val AK_PHONE_MIC = booleanPreferencesKey("ak_phone_mic")
        val AK_FPS = intPreferencesKey("ak_fps")
        val AK_PASSTHROUGH = booleanPreferencesKey("ak_passthrough")

        val PANEL_ENABLED = booleanPreferencesKey("panel_enabled")
        val PANEL_NAV_MEDIA = booleanPreferencesKey("panel_nav_media")
        val PANEL_MINIMAP = booleanPreferencesKey("panel_minimap")
        val PANEL_PRIORITY = stringPreferencesKey("panel_priority")
        val PANEL_ANNOUNCE_5S = booleanPreferencesKey("panel_announce_5s")
        val PANEL_STATS_CARD = booleanPreferencesKey("panel_stats_card")
        val PANEL_AUTO_CYCLE = intPreferencesKey("panel_auto_cycle_sec")
        val PANEL_GRID = booleanPreferencesKey("panel_calibration_grid")
        val SIDE_CAMERA_SIGNAL = booleanPreferencesKey("side_camera_turn_signal")

        val DISMISSED_UPDATE_TAG = stringPreferencesKey("dismissed_update_tag")
        val KNOWN_DEVICES_JSON = stringPreferencesKey("known_devices_json")
    }

    val settingsFlow: Flow<AppSettings> = context.marolLinkDataStore.data.map { prefs ->
        mapPreferencesToSettings(prefs)
    }

    val knownDevicesFlow: Flow<List<KnownDevice>> = context.marolLinkDataStore.data.map { prefs ->
        KnownDevice.deserializeList(prefs[Keys.KNOWN_DEVICES_JSON])
            .sortedByDescending { it.lastConnectedEpochMs }
    }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        context.marolLinkDataStore.edit { prefs ->
            val updated = transform(mapPreferencesToSettings(prefs))
            writeSettingsToPrefs(prefs, updated)
        }
    }

    private fun mapPreferencesToSettings(prefs: Preferences): AppSettings = AppSettings(
        termsAccepted = prefs[Keys.TERMS_ACCEPTED] ?: false,
        onboardingTipsCompleted = prefs[Keys.ONBOARDING_DONE] ?: false,
        autoConnectUsb = prefs[Keys.AUTO_CONNECT_USB] ?: true,
        fullscreenMode = prefs[Keys.FULLSCREEN_MODE] ?: true,
        accentHue = prefs[Keys.ACCENT_HUE] ?: 200f,
        appLanguage = parseEnum(prefs[Keys.APP_LANGUAGE], AppLanguage.PL),
        uiSoundsEnabled = prefs[Keys.UI_SOUNDS] ?: true,
        animationsEnabled = prefs[Keys.ANIMATIONS] ?: true,
        showFpsCounter = prefs[Keys.SHOW_FPS] ?: false,
        floatingControlsEnabled = prefs[Keys.FLOATING_CONTROLS] ?: true,
        safeAreaTopDp = prefs[Keys.SAFE_TOP] ?: 0,
        safeAreaBottomDp = prefs[Keys.SAFE_BOTTOM] ?: 0,
        safeAreaLeftDp = prefs[Keys.SAFE_LEFT] ?: 0,
        safeAreaRightDp = prefs[Keys.SAFE_RIGHT] ?: 0,
        volumeBoostDb = prefs[Keys.VOLUME_BOOST_DB] ?: 0,
        steeringMappings = AppSettings.deserializeSteeringMap(prefs[Keys.STEERING_MAP_JSON]),
        callRecordingFolderUri = prefs[Keys.CALL_REC_FOLDER_URI],
        carPlayResolution = parseEnum(prefs[Keys.CP_RESOLUTION], VideoResolutionPreset.AUTO),
        carPlayFps = prefs[Keys.CP_FPS] ?: 60,
        carPlayHevcEnabled = prefs[Keys.CP_HEVC] ?: false,
        carPlayDpi = prefs[Keys.CP_DPI] ?: 200,
        carPlaySmoothVideo = prefs[Keys.CP_SMOOTH] ?: true,
        carPlayAlacLossless = prefs[Keys.CP_ALAC] ?: true,
        carPlayHighQualityNavVoice = prefs[Keys.CP_HQ_NAV] ?: true,
        carPlayMainBufferedAudio = prefs[Keys.CP_MAIN_BUF] ?: false,
        carPlayAudioViaCarBt = prefs[Keys.CP_AUDIO_BT] ?: false,
        carPlayDisconnectCarBtOnWireless = prefs[Keys.CP_DISCONNECT_BT] ?: false,
        carPlayAudioBufferCableMs = prefs[Keys.CP_BUF_CABLE] ?: 80,
        carPlayAudioBufferWifiMs = prefs[Keys.CP_BUF_WIFI] ?: 180,
        carPlayWirelessMode = parseEnum(prefs[Keys.CP_WIRELESS_MODE], WirelessTransportMode.LOCAL_HOTSPOT),
        aaResolution = parseEnum(prefs[Keys.AA_RESOLUTION], VideoResolutionPreset.FHD_1080),
        aaAutoDpi = prefs[Keys.AA_AUTO_DPI] ?: true,
        aaManualDpi = prefs[Keys.AA_MANUAL_DPI] ?: 200,
        aaFps = prefs[Keys.AA_FPS] ?: 60,
        aaSmoothVideo = prefs[Keys.AA_SMOOTH] ?: true,
        aaMicSource = parseEnum(prefs[Keys.AA_MIC_SOURCE], MicSource.CAR_HEAD_UNIT),
        aaWirelessMode = parseEnum(prefs[Keys.AA_WIRELESS_MODE], WirelessTransportMode.WIFI_DIRECT_5GHZ),
        aaAudioBufferCableMs = prefs[Keys.AA_BUF_CABLE] ?: 80,
        aaAudioBufferWifiMs = prefs[Keys.AA_BUF_WIFI] ?: 180,
        autoKitAutoConnect = prefs[Keys.AK_AUTO_CONNECT] ?: true,
        autoKitAudioViaCarBt = prefs[Keys.AK_AUDIO_BT] ?: false,
        autoKitUsePhoneMic = prefs[Keys.AK_PHONE_MIC] ?: false,
        autoKitFps = prefs[Keys.AK_FPS] ?: 60,
        autoKitPassthroughMode = prefs[Keys.AK_PASSTHROUGH] ?: false,
        panelEnabled = prefs[Keys.PANEL_ENABLED] ?: false,
        panelReceiveNavAndMedia = prefs[Keys.PANEL_NAV_MEDIA] ?: true,
        panelShowMinimap = prefs[Keys.PANEL_MINIMAP] ?: true,
        panelPriority = parseEnum(prefs[Keys.PANEL_PRIORITY], PanelPriority.MAP),
        panelAnnounceTrackChange5s = prefs[Keys.PANEL_ANNOUNCE_5S] ?: true,
        panelShowStatsCard = prefs[Keys.PANEL_STATS_CARD] ?: false,
        panelAutoCycleSeconds = prefs[Keys.PANEL_AUTO_CYCLE] ?: 0,
        panelCalibrationGridEnabled = prefs[Keys.PANEL_GRID] ?: false,
        sideCameraOnTurnSignal = prefs[Keys.SIDE_CAMERA_SIGNAL] ?: false,
        dismissedUpdateTag = prefs[Keys.DISMISSED_UPDATE_TAG]
    )

    private fun writeSettingsToPrefs(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        updated: AppSettings
    ) {
            prefs[Keys.TERMS_ACCEPTED] = updated.termsAccepted
            prefs[Keys.ONBOARDING_DONE] = updated.onboardingTipsCompleted
            prefs[Keys.AUTO_CONNECT_USB] = updated.autoConnectUsb
            prefs[Keys.FULLSCREEN_MODE] = updated.fullscreenMode
            prefs[Keys.ACCENT_HUE] = updated.accentHue
            prefs[Keys.APP_LANGUAGE] = updated.appLanguage.name
            prefs[Keys.UI_SOUNDS] = updated.uiSoundsEnabled
            prefs[Keys.ANIMATIONS] = updated.animationsEnabled
            prefs[Keys.SHOW_FPS] = updated.showFpsCounter
            prefs[Keys.FLOATING_CONTROLS] = updated.floatingControlsEnabled
            prefs[Keys.SAFE_TOP] = updated.safeAreaTopDp
            prefs[Keys.SAFE_BOTTOM] = updated.safeAreaBottomDp
            prefs[Keys.SAFE_LEFT] = updated.safeAreaLeftDp
            prefs[Keys.SAFE_RIGHT] = updated.safeAreaRightDp
            prefs[Keys.VOLUME_BOOST_DB] = updated.volumeBoostDb
            prefs[Keys.STEERING_MAP_JSON] = AppSettings.serializeSteeringMap(updated.steeringMappings)
            if (updated.callRecordingFolderUri != null) {
                prefs[Keys.CALL_REC_FOLDER_URI] = updated.callRecordingFolderUri
            } else {
                prefs.remove(Keys.CALL_REC_FOLDER_URI)
            }
            prefs[Keys.CP_RESOLUTION] = updated.carPlayResolution.name
            prefs[Keys.CP_FPS] = updated.carPlayFps
            prefs[Keys.CP_HEVC] = updated.carPlayHevcEnabled
            prefs[Keys.CP_DPI] = updated.carPlayDpi
            prefs[Keys.CP_SMOOTH] = updated.carPlaySmoothVideo
            prefs[Keys.CP_ALAC] = updated.carPlayAlacLossless
            prefs[Keys.CP_HQ_NAV] = updated.carPlayHighQualityNavVoice
            prefs[Keys.CP_MAIN_BUF] = updated.carPlayMainBufferedAudio
            prefs[Keys.CP_AUDIO_BT] = updated.carPlayAudioViaCarBt
            prefs[Keys.CP_DISCONNECT_BT] = updated.carPlayDisconnectCarBtOnWireless
            prefs[Keys.CP_BUF_CABLE] = updated.carPlayAudioBufferCableMs
            prefs[Keys.CP_BUF_WIFI] = updated.carPlayAudioBufferWifiMs
            prefs[Keys.CP_WIRELESS_MODE] = updated.carPlayWirelessMode.name
            prefs[Keys.AA_RESOLUTION] = updated.aaResolution.name
            prefs[Keys.AA_AUTO_DPI] = updated.aaAutoDpi
            prefs[Keys.AA_MANUAL_DPI] = updated.aaManualDpi
            prefs[Keys.AA_FPS] = updated.aaFps
            prefs[Keys.AA_SMOOTH] = updated.aaSmoothVideo
            prefs[Keys.AA_MIC_SOURCE] = updated.aaMicSource.name
            prefs[Keys.AA_WIRELESS_MODE] = updated.aaWirelessMode.name
            prefs[Keys.AA_BUF_CABLE] = updated.aaAudioBufferCableMs
            prefs[Keys.AA_BUF_WIFI] = updated.aaAudioBufferWifiMs
            prefs[Keys.AK_AUTO_CONNECT] = updated.autoKitAutoConnect
            prefs[Keys.AK_AUDIO_BT] = updated.autoKitAudioViaCarBt
            prefs[Keys.AK_PHONE_MIC] = updated.autoKitUsePhoneMic
            prefs[Keys.AK_FPS] = updated.autoKitFps
            prefs[Keys.AK_PASSTHROUGH] = updated.autoKitPassthroughMode
            prefs[Keys.PANEL_ENABLED] = updated.panelEnabled
            prefs[Keys.PANEL_NAV_MEDIA] = updated.panelReceiveNavAndMedia
            prefs[Keys.PANEL_MINIMAP] = updated.panelShowMinimap
            prefs[Keys.PANEL_PRIORITY] = updated.panelPriority.name
            prefs[Keys.PANEL_ANNOUNCE_5S] = updated.panelAnnounceTrackChange5s
            prefs[Keys.PANEL_STATS_CARD] = updated.panelShowStatsCard
            prefs[Keys.PANEL_AUTO_CYCLE] = updated.panelAutoCycleSeconds
            prefs[Keys.PANEL_GRID] = updated.panelCalibrationGridEnabled
            prefs[Keys.SIDE_CAMERA_SIGNAL] = updated.sideCameraOnTurnSignal
            if (updated.dismissedUpdateTag != null) {
                prefs[Keys.DISMISSED_UPDATE_TAG] = updated.dismissedUpdateTag
            } else {
                prefs.remove(Keys.DISMISSED_UPDATE_TAG)
            }
    }

    suspend fun recordDevice(device: KnownDevice) {
        context.marolLinkDataStore.edit { prefs ->
            val list = KnownDevice.deserializeList(prefs[Keys.KNOWN_DEVICES_JSON]).toMutableList()
            list.removeAll { it.id == device.id }
            list.add(0, device)
            prefs[Keys.KNOWN_DEVICES_JSON] = KnownDevice.serializeList(list.take(20))
        }
    }

    suspend fun removeDevice(deviceId: String) {
        context.marolLinkDataStore.edit { prefs ->
            val filtered = KnownDevice.deserializeList(prefs[Keys.KNOWN_DEVICES_JSON])
                .filterNot { it.id == deviceId }
            prefs[Keys.KNOWN_DEVICES_JSON] = KnownDevice.serializeList(filtered)
        }
    }

    suspend fun clearDevices() {
        context.marolLinkDataStore.edit { prefs ->
            prefs.remove(Keys.KNOWN_DEVICES_JSON)
        }
    }

    /** Restores settings while preserving accepted terms, onboarding and paired phones (per PRD). */
    suspend fun resetSettingsKeepingDevices() {
        context.marolLinkDataStore.edit { prefs ->
            val savedDevices = prefs[Keys.KNOWN_DEVICES_JSON]
            val accepted = prefs[Keys.TERMS_ACCEPTED] ?: true
            val onboarded = prefs[Keys.ONBOARDING_DONE] ?: true
            prefs.clear()
            prefs[Keys.TERMS_ACCEPTED] = accepted
            prefs[Keys.ONBOARDING_DONE] = onboarded
            if (savedDevices != null) {
                prefs[Keys.KNOWN_DEVICES_JSON] = savedDevices
            }
        }
    }

    private inline fun <reified T : Enum<T>> parseEnum(raw: String?, fallback: T): T {
        if (raw.isNullOrBlank()) return fallback
        return runCatching { enumValueOf<T>(raw) }.getOrDefault(fallback)
    }
}
