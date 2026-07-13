package science.credo.mobiledetector.fragment

import android.graphics.*
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import science.credo.mobiledetector.R
import science.credo.mobiledetector.fragment.detections.DetectionContent

class DetectionAdapter(
        private val mValues: List<DetectionContent.HitItem>,
        private val mListener: DetectionFragment.OnListFragmentInteractionListener?
) : RecyclerView.Adapter<DetectionAdapter.ViewHolder>() {

    private val invertedStates = mutableMapOf<String, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_detection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mItem = item
        holder.mIdView.text = item.id
        holder.mContentView.text = item.content
        
        val dataString = try {
            Base64.decode(item.frame, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }

        if (dataString != null) {
            val img = BitmapFactory.decodeByteArray(dataString, 0, dataString.size)
            if (img != null) {
                val itemId = item.id
                val isInverted = invertedStates[itemId] ?: false
                
                updateImage(holder.mHit, img, isInverted)
                
                holder.mInvertButton.setOnClickListener {
                    val newState = !(invertedStates[itemId] ?: false)
                    invertedStates[itemId] = newState
                    updateImage(holder.mHit, img, newState)
                }
                
                holder.mSizeView.text = holder.mSizeView.context.getString(R.string.detections_item_size, img.width, img.height)
            } else {
                holder.mHit.setImageResource(R.drawable.ic_info_black_24dp)
                holder.mSizeView.text = "Invalid image"
            }
        } else {
            holder.mHit.setImageResource(R.drawable.ic_info_black_24dp)
            holder.mSizeView.text = "No image data"
        }

        val hit = item.hit
        holder.mPositionView.text = holder.mPositionView.context.getString(R.string.detections_item_pos, hit.mX, hit.mY)
        holder.mMaxView.text = holder.mMaxView.context.getString(R.string.detections_item_max, hit.mMaxValue)
        holder.mAverageView.text = holder.mAverageView.context.getString(R.string.detections_item_average, hit.mAverage)
        holder.mBlacksView.text = holder.mBlacksView.context.getString(R.string.detections_item_blacks, hit.mBlacks, hit.mBlackThreshold)
        holder.mAccView.text = holder.mAccView.context.getString(R.string.detections_item_acc, hit.mAx, hit.mAy, hit.mAz)
        holder.mOrientationView.text = holder.mOrientationView.context.getString(R.string.detections_item_orientation, hit.mOrientation)

        holder.mView.setOnClickListener {
            mListener?.onListFragmentInteraction(holder.mItem)
        }
    }

    private fun updateImage(imageView: ImageView, bitmap: Bitmap, invert: Boolean) {
        if (invert) {
            val paint = Paint()
            val matrix = ColorMatrix(floatArrayOf(
                -1.0f, 0f, 0f, 0f, 255f, // Red
                0f, -1.0f, 0f, 0f, 255f, // Green
                0f, 0f, -1.0f, 0f, 255f, // Blue
                0f, 0f, 0f, 1.0f, 0f     // Alpha
            ))
            paint.colorFilter = ColorMatrixColorFilter(matrix)
            imageView.colorFilter = paint.colorFilter
            imageView.setBackgroundColor(Color.WHITE)
        } else {
            imageView.colorFilter = null
            imageView.setBackgroundColor(Color.BLACK)
        }
        imageView.setImageBitmap(bitmap)
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.findViewById(R.id.pk)
        val mContentView: TextView = mView.findViewById(R.id.content)
        val mSizeView: TextView = mView.findViewById(R.id.size)
        val mPositionView: TextView = mView.findViewById(R.id.position)
        val mMaxView: TextView = mView.findViewById(R.id.maxBright)
        val mAverageView: TextView = mView.findViewById(R.id.average)
        val mBlacksView: TextView = mView.findViewById(R.id.blacks)
        val mAccView: TextView = mView.findViewById(R.id.acc)
        val mOrientationView: TextView = mView.findViewById(R.id.orientation)
        val mHit: ImageView = mView.findViewById(R.id.hit)
        val mInvertButton: ImageButton = mView.findViewById(R.id.invert_button)
        var mItem: DetectionContent.HitItem? = null
    }
}
