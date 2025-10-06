package moe.matsuri.nb4a.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.preference.PreferenceViewHolder
import com.takisoft.preferencex.SimpleMenuPreference
import io.nekohasekai.sagernet.R

class LongClickMenuPreference : SimpleMenuPreference {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    )

    constructor(
        context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)
    private var mLongClickListener: View.OnLongClickListener? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val itemView: View = holder.itemView
        itemView.setOnLongClickListener {
            mLongClickListener?.onLongClick(it) ?: true
        }
    }

    fun setOnLongClickListener(longClickListener: View.OnLongClickListener) {
        this.mLongClickListener = longClickListener
    }

}
