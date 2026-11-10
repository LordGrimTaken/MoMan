package eu.kanade.tachiyomi.ui.reader.viewer

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.R
import com.google.android.material.button.MaterialButton
import eu.kanade.tachiyomi.ui.reader.viewer.pager.PagerViewer

/**
 * A button class to be used by child views of the pager viewer. All tap gestures are handled by
 * the pager, but this class disables that behavior to allow clickable buttons.
 */
class ReaderButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialButtonStyle,
) : MaterialButton(context, attrs, defStyleAttr) {

    var viewer: PagerViewer? = null

    // This is a real button: super.onTouchEvent already dispatches the click on ACTION_UP, so calling
    // performClick() here would fire the listener twice. The override below keeps the a11y entry point open.
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        viewer?.pager?.setGestureDetectorEnabled(false)
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            viewer?.pager?.setGestureDetectorEnabled(true)
        }
        return super.onTouchEvent(event)
    }

    /**
     * Accessibility services activate a touch-handling view through performClick(); without this override the
     * view is unreachable for them even though it reacts to touch (lint: ClickableViewAccessibility).
     */
    override fun performClick(): Boolean = super.performClick()

}
