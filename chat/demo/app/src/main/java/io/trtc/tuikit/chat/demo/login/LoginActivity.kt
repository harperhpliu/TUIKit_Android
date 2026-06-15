package io.trtc.tuikit.chat.demo.login

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.tencent.cloud.tuikit.engine.call.TUICallEngine
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.mmkv.MMKV
import com.tencent.qcloud.tuikit.tuicallkit.TUICallKit
import com.tencent.qcloud.tuikit.tuicallkit.manager.feature.CallingBellFeature
import com.tencent.qcloud.tuikit.tuicallkit.manager.feature.CallingVibratorFeature
import com.tencent.qcloud.tuikit.tuicallkit.manager.feature.NotificationFeature
import io.trtc.tuikit.atomicx.theme.Theme
import io.trtc.tuikit.atomicx.theme.ThemeStore
import io.trtc.tuikit.atomicx.theme.tokens.ColorTokens
import io.trtc.tuikit.chat.uikit.components.widgets.ActionItem
import io.trtc.tuikit.chat.uikit.components.widgets.ActionSheet
import io.trtc.tuikit.atomicxcore.api.CompletionHandler
import io.trtc.tuikit.atomicxcore.api.login.LoginStore
import io.trtc.tuikit.chat.demo.AppConstants
import io.trtc.tuikit.chat.demo.BaseActivity
import io.trtc.tuikit.chat.demo.MainActivity
import io.trtc.tuikit.chat.app.R
import io.trtc.tuikit.chat.demo.signature.GenerateTestUserSig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "LoginActivity"

class LoginActivity : BaseActivity() {

    private lateinit var loginRoot: LinearLayout
    private lateinit var loginPanel: FrameLayout
    private lateinit var heroSection: FrameLayout

    private lateinit var themeSwitcher: LinearLayout
    private lateinit var tvThemeValue: TextView
    private lateinit var languageSwitcher: LinearLayout
    private lateinit var tvLanguageValue: TextView

    private lateinit var tvUserIdLabel: TextView
    private lateinit var editUserId: EditText
    private lateinit var editDivider: View
    private lateinit var btnLogin: Button

    private val themeStore by lazy { ThemeStore.shared(this) }
    private var loginScope: CoroutineScope? = null

    private val appLanguageOptions by lazy {
        listOf(
            ActionItem(text = getString(R.string.settings_zh_hans), value = "zh"),
            ActionItem(text = getString(R.string.settings_zh_hant), value = "zh-Hant"),
            ActionItem(text = getString(R.string.settings_en), value = "en"),
            ActionItem(text = getString(R.string.settings_ar), value = "ar")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loginUser = MMKV.defaultMMKV().decodeString(AppConstants.KEY_LOGIN_USER, "")
        if (!loginUser.isNullOrEmpty()) {
            login(loginUser)
            return
        }

        setContentView(R.layout.activity_login)

        bindViews()
        updateThemeSwitcherLabel()
        updateLanguageSwitcherLabel()
        applyThemeColors(themeStore.themeState.value.currentTheme.tokens.color)
        setupLoginInteractions()
        setupThemeSwitcher()
        setupLanguageSwitcher()
    }

    override fun onStart() {
        super.onStart()
        if (!isLoginViewReady()) {
            return
        }
        loginScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        loginScope?.launch {
            themeStore.themeState.collectLatest { state ->
                applyThemeColors(state.currentTheme.tokens.color)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        loginScope?.cancel()
        loginScope = null
    }

    private fun bindViews() {
        loginRoot = findViewById(R.id.loginRoot)
        loginPanel = findViewById(R.id.loginPanel)
        heroSection = findViewById(R.id.heroSection)

        themeSwitcher = findViewById(R.id.themeSwitcher)
        tvThemeValue = findViewById(R.id.tvThemeValue)
        languageSwitcher = findViewById(R.id.languageSwitcher)
        tvLanguageValue = findViewById(R.id.tvLanguageValue)

        tvUserIdLabel = findViewById(R.id.tvUserIdLabel)
        editUserId = findViewById(R.id.editUserID)
        editDivider = findViewById(R.id.editDivider)
        btnLogin = findViewById(R.id.btnLogin)
    }

    private fun setupLoginInteractions() {
        updateLoginButtonState(editUserId.text)
        editUserId.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateLoginButtonState(s)
                }

                override fun afterTextChanged(s: Editable?) {}
            }
        )
        editUserId.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && btnLogin.isEnabled) {
                btnLogin.performClick()
                true
            } else {
                false
            }
        }

        btnLogin.setOnClickListener {
            val userId = editUserId.text.toString().trim()
            if (userId.isNotEmpty()) {
                login(userId)
            }
        }
    }

    private fun setupThemeSwitcher() {
        themeSwitcher.setOnClickListener { showThemeSelector() }
    }

    private fun setupLanguageSwitcher() {
        languageSwitcher.setOnClickListener { showLanguageSelector() }
    }

    private fun updateLoginButtonState(input: CharSequence?) {
        btnLogin.isEnabled = input?.toString()?.trim()?.isNotEmpty() == true
    }

    private fun isLoginViewReady(): Boolean {
        return ::loginRoot.isInitialized &&
                ::loginPanel.isInitialized &&
                ::editUserId.isInitialized &&
                ::btnLogin.isInitialized
    }

    private fun applyThemeColors(colors: ColorTokens) {
        if (!isLoginViewReady()) {
            return
        }

        loginRoot.setBackgroundColor(colors.bgColorOperate)
        loginPanel.setBackgroundColor(colors.bgColorOperate)

        tvUserIdLabel.setTextColor(colors.textColorPrimary)
        editUserId.setTextColor(colors.textColorPrimary)
        editUserId.setHintTextColor(colors.textColorTertiary)
        editDivider.setBackgroundColor(colors.strokeColorPrimary)

        updateBackgroundPreservingPadding(btnLogin, createButtonBackground(colors))
        btnLogin.setTextColor(createButtonTextColors(colors))
    }

    private fun createButtonBackground(colors: ColorTokens): StateListDrawable {
        return StateListDrawable().apply {
            addState(
                intArrayOf(-android.R.attr.state_enabled),
                createButtonShape(colors.buttonColorPrimaryDisabled)
            )
            addState(
                intArrayOf(android.R.attr.state_pressed),
                createButtonShape(colors.buttonColorPrimaryActive)
            )
            addState(
                intArrayOf(),
                createButtonShape(colors.buttonColorPrimaryDefault)
            )
        }
    }

    private fun createButtonShape(fillColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fillColor)
            cornerRadius = dpToPx(14).toFloat()
        }
    }

    private fun createButtonTextColors(colors: ColorTokens): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf()
            ),
            intArrayOf(
                colors.textColorButtonDisabled,
                colors.textColorButton
            )
        )
    }

    private fun updateBackgroundPreservingPadding(view: View, background: Drawable) {
        val paddingStart = view.paddingStart
        val paddingTop = view.paddingTop
        val paddingEnd = view.paddingEnd
        val paddingBottom = view.paddingBottom
        view.background = background
        view.setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
    }

    private fun dpToPx(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun showThemeSelector() {
        val options = listOf(
            ActionItem(text = getString(R.string.settings_theme_system), value = AppConstants.THEME_MODE_SYSTEM),
            ActionItem(text = getString(R.string.settings_theme_light), value = AppConstants.THEME_MODE_LIGHT),
            ActionItem(text = getString(R.string.settings_theme_dark), value = AppConstants.THEME_MODE_DARK)
        )
        ActionSheet.show(this, options) { selected ->
            val mode = selected.value as Int
            MMKV.defaultMMKV().encode(AppConstants.KEY_THEME_MODE, mode)
            when (mode) {
                AppConstants.THEME_MODE_SYSTEM -> {
                    val isNight = (resources.configuration.uiMode and
                            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                    themeStore.setTheme(
                        if (isNight) Theme.darkTheme(this) else Theme.lightTheme(this)
                    )
                }

                AppConstants.THEME_MODE_LIGHT -> themeStore.setTheme(Theme.lightTheme(this))
                AppConstants.THEME_MODE_DARK -> themeStore.setTheme(Theme.darkTheme(this))
            }
            updateThemeSwitcherLabel()
        }
    }

    private fun showLanguageSelector() {
        ActionSheet.show(this, appLanguageOptions) { selected ->
            val tag = selected.value as String
            val targetLocales = LocaleListCompat.forLanguageTags(tag)
            MMKV.defaultMMKV().encode(AppConstants.KEY_APP_LANGUAGE, tag)
            if (AppCompatDelegate.getApplicationLocales() == targetLocales) {
                return@show
            }
            AppCompatDelegate.setApplicationLocales(targetLocales)
        }
    }

    private fun updateThemeSwitcherLabel() {
        val savedThemeMode = MMKV.defaultMMKV()
            .decodeInt(AppConstants.KEY_THEME_MODE, AppConstants.THEME_MODE_SYSTEM)
        tvThemeValue.text = when (savedThemeMode) {
            AppConstants.THEME_MODE_LIGHT -> getString(R.string.settings_theme_light)
            AppConstants.THEME_MODE_DARK -> getString(R.string.settings_theme_dark)
            else -> getString(R.string.settings_theme_system)
        }
    }

    private fun updateLanguageSwitcherLabel() {
        tvLanguageValue.text = getCurrentLanguageDisplayName()
    }

    private fun getCurrentLanguageDisplayName(): String {
        val persistedTag = MMKV.defaultMMKV()
            .decodeString(AppConstants.KEY_APP_LANGUAGE, "").orEmpty()
        val currentTag = if (persistedTag.isNotBlank()) {
            persistedTag
        } else {
            AppCompatDelegate.getApplicationLocales().toLanguageTags()
        }
        return when {
            currentTag.isBlank() -> getString(R.string.settings_current_language)
            isTraditionalChinese(currentTag) -> getString(R.string.settings_zh_hant)
            currentTag.startsWith("zh", ignoreCase = true) -> getString(R.string.settings_zh_hans)
            currentTag.startsWith("en", ignoreCase = true) -> getString(R.string.settings_en)
            currentTag.startsWith("ar", ignoreCase = true) -> getString(R.string.settings_ar)
            else -> getString(R.string.settings_current_language)
        }
    }

    private fun isTraditionalChinese(languageTag: String): Boolean {
        val normalizedTag = languageTag.lowercase()
        return normalizedTag.contains("hant") ||
                normalizedTag.contains("zh-hk") ||
                normalizedTag.contains("zh-tw") ||
                normalizedTag.contains("zh-mo")
    }

    private fun login(userID: String) {
        val sdkAppId = GenerateTestUserSig.SDKAPPID
        val userId = userID
        val userSig = GenerateTestUserSig.genTestUserSig(userID)
        LoginStore.shared.login(
            this,
            sdkAppId, userID, userSig,
            object : CompletionHandler {
                override fun onSuccess() {
                    initCall(sdkAppId, userId, userSig)
                    MMKV.defaultMMKV().encode(AppConstants.KEY_LOGIN_USER, userID)
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }

                override fun onFailure(code: Int, desc: String) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed: $desc",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun LoginActivity.initCall(
        sdkAppId: Int,
        userId: String,
        userSig: String?
    ) {
        TUICallEngine.createInstance(this).init(
            sdkAppId, userId, userSig,
            object : TUICommonDefine.Callback {
                override fun onSuccess() {
                    Log.i(TAG, "callEngine init success")
                    val notificationFeature = NotificationFeature(this@LoginActivity)
                    notificationFeature.registerNotificationBannerChannel()
                    CallingBellFeature(this@LoginActivity)
                    CallingVibratorFeature(this@LoginActivity)
                    TUICallEngine.createInstance(this@LoginActivity).enableMultiDeviceAbility(true, null)
                    TUICallKit.createInstance(this@LoginActivity).enableIncomingBanner(true)
                }

                override fun onError(errCode: Int, errMsg: String) {
                    Log.e(TAG, "callEngine init failed, errCode: $errCode, errMsg: $errMsg")
                }
            })
    }
}