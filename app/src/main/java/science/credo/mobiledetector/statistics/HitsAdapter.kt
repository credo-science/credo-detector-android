package science.credo.mobiledetector.statistics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import science.credo.mobiledetector.R
import science.credo.mobiledetector.detector.Hit
import science.credo.mobiledetector.utils.BitmapUtils
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class HitsAdapter(
    val context: Context,
    val items: Array<Hit>,
    val onClickListener: OnClickListener,
    val itemHeight: Int
) : RecyclerView.Adapter<HitsAdapter.VH>() {
    val inflater = LayoutInflater.from(context)

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    interface OnClickListener {
        fun onClick(hit: Hit)
    }

    class VH(v: View, itemHeight: Int) : RecyclerView.ViewHolder(v) {
        val ivHit = v.findViewById<ImageView>(R.id.ivHit)
        val progressBar = v.findViewById<ProgressBar>(R.id.progressBar)
        val tvTime = v.findViewById<TextView>(R.id.tvTime)

        init {
            v.layoutParams.height = itemHeight
//            val m = Matrix()
//            m.setScale(5f, 5f)
//            ivHit.imageMatrix = m
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(inflater.inflate(R.layout.item_hit, parent, false), itemHeight)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            holder.progressBar.visibility = View.VISIBLE
            val b = BitmapUtils.loadBitmap(items[position].frameContent)
            holder.ivHit.setImageBitmap(b)
            holder.ivHit.tag = b
            holder.progressBar.visibility = View.GONE
            val cal = Calendar.getInstance()
            cal.timeInMillis = items[position].timestamp ?: 0L
            val timeParts = sdf.format(cal.time).split(" ")
            holder.tvTime.text = "${timeParts[0]}\n${timeParts[1]}"
        }
        holder.itemView.setOnClickListener {
            onClickListener.onClick(items[position])
        }
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        try {
            (holder.ivHit.tag as Bitmap).recycle()
        } catch (e: Exception) {
        }
    }


}