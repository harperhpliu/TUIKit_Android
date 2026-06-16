package io.trtc.tuikit.chat.demo

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tencent.mmkv.MMKV
import io.trtc.tuikit.atomicx.theme.Theme
import io.trtc.tuikit.atomicx.theme.ThemeStore
import io.trtc.tuikit.atomicx.theme.tokens.ColorTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseActivity : AppCompatActivity() {

    private val themeStore by lazy { ThemeStore.shared(this) }
    private var themeScope: CoroutineScope? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        updateWindowAppearance(themeStore.themeState.value.currentTheme.tokens.color)
    }

    override fun onStart() {
        super.onStart()
        themeScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        themeScope?.launch {
            themeStore.themeState.collectLatest { state ->
                updateWindowAppearance(state.currentTheme.tokens.color)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        themeScope?.cancel()
        themeScope = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val themeMode = MMKV.defaultMMKV().decodeInt(
            AppConstants.KEY_THEME_MODE,
            AppConstants.THEME_MODE_SYSTEM
        )
        if (themeMode == AppConstants.THEME_MODE_SYSTEM) {
            val isNight = (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
            themeStore.setTheme(
                if (isNight) Theme.darkTheme(this) else Theme.lightTheme(this)
            )
        }
    }

    private fun updateWindowAppearance(colors: ColorTokens) {
        window.setBackgroundDrawable(ColorDrawable(colors.bgColorDefault))
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        val isLight = isColorLight(colors.bgColorOperate)
        controller.isAppearanceLightStatusBars = isLight
        controller.isAppearanceLightNavigationBars = isLight
    }

    private fun isColorLight(color: Int): Boolean {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
        return luminance > 0.5
    }
}
