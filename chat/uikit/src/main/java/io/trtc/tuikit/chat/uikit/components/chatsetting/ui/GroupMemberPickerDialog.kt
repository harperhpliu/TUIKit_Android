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
import io.trtc.tuikit.chat.uikit.components.common.expandTouchTarget
import io.trtc.tuikit.atomicx.common.util.ScreenUtil.dp2px
import io.trtc.tuikit.chat.uikit.components.chatsetting.utils.WindowThemeUtil
import io.trtc.tuikit.atomicx.theme.ThemeStore
import io.trtc.tuikit.atomicx.theme.tokens.ColorTokens
import io.trtc.tuikit.chat.uikit.components.userpicker.model.UserPickerData
import io.trtc.tuikit.chat.uikit.components.userpicker.ui.UserPickerView
import io.trtc.tuikit.atomicxcore.api.group.GroupMember

internal class GroupMemberPickerDialog(
    private val context: Context,
    private val title: String,
    private val candidates: List<GroupMember>,
    private val preSelectedMemberIDs: List<String> = emptyList(),
    private val maxSelection: Int = Int.MAX_VALUE,
    private val onConfirm: (List<GroupMember>) -> Unit
) {

    private var dialog: Dialog? = null
    private var selectedMembers: List<GroupMember> = emptyList()
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
            setDefaultSelectedItems(preSelectedMemberIDs)
            setDataSource(
                candidates.map { member ->
                    UserPickerData(
                        key = member.userID,
                        label = member.displayName,
                        avatarUrl = member.avatarURL,
                        extraData = member
                    )
                }
            )
            setOnSelectedChangedListener<GroupMember> { selectedItems ->
                selectedMembers = selectedItems.map { it.extraData }
                updateConfirmState(colors)
            }
        }
        rootLayout.addView(pickerView)

        selectedMembers = candidates.filter { preSelectedMemberIDs.contains(it.userID) }

        dialog?.setContentView(rootLayout)
        dialog?.show()
        updateConfirmState(colors)
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    private fun buildTopBar(colors: ColorTokens, dm: android.util.DisplayMetrics): FrameLayout {
        return FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp2px(56f, dm).toInt()
            )
            setBackgroundColor(colors.bgColorOperate)
            setPadding(dp2px(10f, dm).toInt(), 0, dp2px(10f, dm).toInt(), 0)

            val cancelView = TextView(context).apply {
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
            addView(cancelView)
            cancelView.expandTouchTarget()

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
                    onConfirm(selectedMembers)
                    dialog?.dismiss()
                }
            }
            addView(confirmView)
            confirmView.expandTouchTarget()
        }
    }

    private fun updateConfirmState(colors: ColorTokens) {
        confirmView.setTextColor(colors.textColorLink)
    }

    private fun getColors(): ColorTokens {
        return ThemeStore.shared(context).themeState.value.currentTheme.tokens.color
    }

    private val GroupMember.displayName: String
        get() = when {
            !nameCard.isNullOrEmpty() -> nameCard!!
            !nickname.isNullOrEmpty() -> nickname!!
            else -> userID
        }
}
