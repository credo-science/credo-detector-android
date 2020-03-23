package science.credo.mobiledetector.intro


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import science.credo.mobiledetector.R

class IntroPagerAdapter :
    ListAdapter<Int, IntroPagerAdapter.Companion.EmptyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EmptyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.empty_match_parent_layout,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: EmptyViewHolder, position: Int) {}

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem

            override fun areContentsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
        }

        class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}