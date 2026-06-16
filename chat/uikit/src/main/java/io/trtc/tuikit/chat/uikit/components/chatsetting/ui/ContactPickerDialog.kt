package io.trtc.tuikit.chat.uikit.components.chatsetting.ui
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import io.trtc.tuikit.chat.uikit.R
import io.trtc.tuikit.atomicx.common.util.ScreenUtil.dp2px
import io.trtc.tuikit.chat.uikit.components.chatsetting.utils.WindowThemeUtil
import io.trtc.tuikit.chat.uikit.components.contactlist.utils.displayName
import io.trtc.tuikit.atomicx.theme.ThemeStore
import io.trtc.tuikit.atomicx.theme.tokens.ColorTokens
import io.trtc.tuikit.chat.uikit.components.userpicker.model.UserPickerData
import io.trtc.tuikit.chat.uikit.components.userpicker.ui.UserPickerView
import io.trtc.tuikit.atomicxcore.api.contact.ContactInfo

internal class ContactPickerDialog(
    private val context: Context,
    private val title: String,
    private val contacts: List<ContactInfo>,
    private val maxSelection: Int = 100,
    private val preSelectedUserIds: List<String> = emptyList(),
    private val allowEmptyConfirm: Boolean = false,
    private val onConfirm: (List<ContactInfo>) -> Unit
) {

    private var dialog: Dialog? = null
    private var selectedContacts: List<ContactInfo> = emptyList()
    private lateinit var confirmView: TextView

    fun show() {
        val colors = getColors()
        val dm = context.resources.displayMetrics

        dialog = Dialog(context, android.R.style.Theme_NoTitleBar).apply {
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                WindowThemeUtil.applyDialogSystemBarStyle(this, colors)
            }
        }

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            setBackgroundColor(colors.bgColorTopBar)
            fitsSystemWindows = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        rootLayout.addView(buildTopBar(colors, dm))

        val pickerView = UserPickerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setMaxCount(maxSelection)
            setDataSource(
                contacts.map { contact ->
                    UserPickerData(
                        key = contact.userID,
                        label = contact.displayName,
                        avatarUrl = contact.avatarURL,
                        extraData = contact
                    )
                }
            )
            setOnSelectedChangedListener<ContactInfo> { selectedItems ->
                selectedContacts = selectedItems.map { it.extraData }
                updateConfirmState(colors)
            }
            if (preSelectedUserIds.isNotEmpty()) {
                setDefaultSelectedItems(preSelectedUserIds)
                selectedContacts = contacts.filter { preSelectedUserIds.contains(it.userID) }
            }
        }
        rootLayout.addView(pickerView)

        dialog?.setContentView(rootLayout)
        dialog?.show()
        updateConfirmState(colors)
    }

    private fun buildTopBar(colors: ColorTokens, dm: android.util.DisplayMetrics): FrameLayout {
        return FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp2px(56f, dm).toInt()
            )
            setBackgroundColor(colors.bgColorOperate)
            setPadding(dp2px(10f, dm).toInt(), 0, dp2px(10f, dm).toInt(), 0)

            addView(
                TextView(context).apply {
                    text = context.getString(R.string.uikit_cancel)
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
                    setTextColor(colors.textColorLink)
                    gravity = Gravity.CENTER_VERTICAL
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        Gravity.START or Gravity.CENTER_VERTICAL
                    )
                    isClickable = true
                    isFocusable = true
                    setOnClickListener { dialog?.dismiss() }
                }
            )

            addView(
                TextView(context).apply {
                    text = title
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
                    setTextColor(colors.textColorPrimary)
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    gravity = Gravity.CENTER
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                    )
                }
            )

            confirmView = TextView(context).apply {
                text = context.getString(R.string.uikit_confirm)
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.END or Gravity.CENTER_VERTICAL
                )
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    if (selectedContacts.isEmpty() && !allowEmptyConfirm) return@setOnClickListener
                    onConfirm(selectedContacts)
                    dialog?.dismiss()
                }
            }
            addView(confirmView)
        }
    }

    private fun updateConfirmState(colors: ColorTokens) {
        val enabled = selectedContacts.isNotEmpty() || allowEmptyConfirm
        confirmView.setTextColor(
            if (enabled) colors.textColorLink else colors.textColorDisable
        )
    }

    private fun getColors(): ColorTokens {
        return ThemeStore.shared(context).themeState.value.currentTheme.tokens.color
    }
}
