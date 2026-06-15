package io.trtc.tuikit.chat.uikit.components.contactlist.ui
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.trtc.tuikit.chat.uikit.R
import io.trtc.tuikit.atomicx.common.util.ScreenUtil.dp2px
import io.trtc.tuikit.chat.uikit.components.contactlist.utils.WindowThemeUtil
import io.trtc.tuikit.chat.uikit.components.common.expandTouchTarget
import io.trtc.tuikit.atomicx.theme.ThemeStore
import io.trtc.tuikit.atomicx.theme.tokens.ColorTokens

internal abstract class ContactSubPageDialog(context: Context) : Dialog(context, android.R.style.Theme_NoTitleBar) {

    protected lateinit var rootLayout: LinearLayout
    protected lateinit var contentContainer: FrameLayout
    private lateinit var titleView: TextView
    private lateinit var backIconView: ImageView
    private lateinit var dividerView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val colors = getColors()
        val dm = context.resources.displayMetrics

        rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            setBackgroundColor(colors.bgColorOperate)
            fitsSystemWindows = true
        }

        val navBarHeight = dp2px(56f, dm).toInt()
        val navBarHPad = dp2px(16f, dm).toInt()

        val navBar = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                navBarHeight
            )
            layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            setPadding(navBarHPad, 0, navBarHPad, 0)
            setBackgroundColor(colors.bgColorOperate)
        }

        val backRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.START or Gravity.CENTER_VERTICAL
            )
            contentDescription = context.getString(R.string.contact_list_back)
        }

        val iconSize = dp2px(16f, dm).toInt()
        backIconView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
            setImageResource(R.drawable.uikit_ic_back)
            imageTintList = ColorStateList.valueOf(colors.textColorSecondary)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        backRow.addView(backIconView)

        backRow.setOnClickListener { dismiss() }
        navBar.addView(backRow)
        backRow.expandTouchTarget()

        titleView = TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(colors.textColorPrimary)
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        }
        navBar.addView(titleView)

        rootLayout.addView(navBar)

        dividerView = View(context).apply {
            setBackgroundColor(colors.strokeColorSecondary)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp2px(0.5f, dm).toInt().coerceAtLeast(1)
            )
        }
        rootLayout.addView(dividerView)

        contentContainer = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        rootLayout.addView(contentContainer)

        setContentView(rootLayout, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))

        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            WindowThemeUtil.applyDialogSystemBarStyle(this, colors)
        }
    }

    protected fun setTitle(title: String) {
        titleView.text = title
    }

    protected fun refreshNavBarColors(colors: ColorTokens) {
        rootLayout.setBackgroundColor(colors.bgColorOperate)
        titleView.setTextColor(colors.textColorPrimary)
        backIconView.imageTintList = ColorStateList.valueOf(colors.textColorSecondary)
        dividerView.setBackgroundColor(colors.strokeColorSecondary)
        window?.let { WindowThemeUtil.applyDialogSystemBarStyle(it, colors) }
    }

    protected fun getColors(): ColorTokens {
        return ThemeStore.shared(context).themeState.value.currentTheme.tokens.color
    }
}
