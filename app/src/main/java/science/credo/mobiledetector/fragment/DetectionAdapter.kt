package science.credo.mobiledetector.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_detection.view.*
import science.credo.mobiledetector.R
import science.credo.mobiledetector.fragment.detections.DetectionContent

class DetectionAdapter(
        private val mValues: List<DetectionContent.HitItem>,
        private val mListener: DetectionFragment.OnListFragmentInteractionListener?
) : RecyclerView.Adapter<DetectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_detection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mIdView.text = holder.mItem!!.id
        holder.mContentView.text = holder.mItem!!.content
        val dataString = Base64.decode(holder.mItem!!.frame, Base64.DEFAULT)

        val img = BitmapFactory.decodeByteArray(dataString, 0, dataString.size)

        var scaleFactor = 2
        while (img.width * scaleFactor < 640) {
            scaleFactor *= 2
        }

        val hit = holder.mItem!!.hit

        try {
            holder.mHit.setImageBitmap(Bitmap.createScaledBitmap(img, img.width * scaleFactor, img.height * scaleFactor, false))
        } catch (t: Throwable) {}

        holder.mSizeView.text = holder.mSizeView.context.getString(R.string.detections_item_size, img.width, img.height)
        holder.mPositionView.text = holder.mSizeView.context.getString(R.string.detections_item_pos, hit.mX, hit.mY)
        holder.mMaxView.text = holder.mSizeView.context.getString(R.string.detections_item_max, hit.mMetadata.mMaxValue)
        holder.mAverageView.text = holder.mSizeView.context.getString(R.string.detections_item_average, hit.mMetadata.mAverage)
        holder.mBlacksView.text = holder.mSizeView.context.getString(R.string.detections_item_blacks, hit.mMetadata.mBlacks, hit.mMetadata.mBlackThreshold)
        holder.mAccView.text = holder.mSizeView.context.getString(R.string.detections_item_acc, hit.mMetadata.mAx, hit.mMetadata.mAy, hit.mMetadata.mAz)
        holder.mOrientationView.text = holder.mSizeView.context.getString(R.string.detections_item_orientation, hit.mMetadata.mOrientation)

        holder.mView.setOnClickListener {
            mListener?.onListFragmentInteraction(holder.mItem)
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.pk
        val mContentView: TextView = mView.content
        val mSizeView: TextView = mView.size
        val mPositionView: TextView = mView.position
        val mMaxView: TextView = mView.maxBright
        val mAverageView: TextView = mView.average
        val mBlacksView: TextView = mView.blacks
        val mAccView: TextView = mView.acc
        val mOrientationView: TextView = mView.orientation
        val mHit: ImageView = mView.hit
        var mItem: DetectionContent.HitItem? = null
    }
}
