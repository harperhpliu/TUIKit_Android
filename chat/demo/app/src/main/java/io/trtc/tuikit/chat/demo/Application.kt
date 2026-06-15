package io.trtc.tuikit.chat.demo

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.tencent.mmkv.MMKV
import io.trtc.tuikit.chat.uikit.components.config.AppBuilder
import io.trtc.tuikit.chat.uikit.components.config.AppBuilderConfig
import io.trtc.tuikit.atomicx.theme.Theme
import io.trtc.tuikit.atomicx.theme.ThemeStore

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        AppBuilder.loadConfig(this)

        applyLanguageFromSettings()

        MMKV.defaultMMKV().decodeBool(AppConstants.KEY_ENABLE_READ_RECEIPT, false).also {
            AppBuilderConfig.enableReadReceipt = it
        }

        applyThemeFromSettings()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val themeMode = MMKV.defaultMMKV().decodeInt(AppConstants.KEY_THEME_MODE, AppConstants.THEME_MODE_SYSTEM)
        if (themeMode == AppConstants.THEME_MODE_SYSTEM) {
            applySystemTheme(newConfig)
        }
    }

    private fun applyThemeFromSettings() {
        val themeMode = MMKV.defaultMMKV().decodeInt(AppConstants.KEY_THEME_MODE, AppConstants.THEME_MODE_SYSTEM)
        val themeStore = ThemeStore.shared(this)
        when (themeMode) {
            AppConstants.THEME_MODE_LIGHT -> themeStore.setTheme(Theme.lightTheme(this))
            AppConstants.THEME_MODE_DARK -> themeStore.setTheme(Theme.darkTheme(this))
            else -> applySystemTheme(resources.configuration)
        }
    }

    private fun applyLanguageFromSettings() {
        val languageTag = MMKV.defaultMMKV().decodeString(AppConstants.KEY_APP_LANGUAGE, "").orEmpty()
        val targetLocales = if (languageTag.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        if (AppCompatDelegate.getApplicationLocales() != targetLocales) {
            AppCompatDelegate.setApplicationLocales(targetLocales)
        }
    }

    private fun applySystemTheme(config: Configuration) {
        val isNight = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        ThemeStore.shared(this).setTheme(
            if (isNight) Theme.darkTheme(this) else Theme.lightTheme(this)
        )
    }
}
