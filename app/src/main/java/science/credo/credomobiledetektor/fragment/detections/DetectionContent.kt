package science.credo.credomobiledetektor.fragment.detections

import android.content.Context
import android.text.format.DateFormat
import science.credo.credomobiledetektor.R
import science.credo.credomobiledetektor.database.DataManager
import science.credo.credomobiledetektor.detection.Hit
import java.text.SimpleDateFormat
import java.util.*

object DetectionContent {

    private val DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH)

    val ITEMS: ArrayList<HitItem> = ArrayList()

    val ITEM_MAP: MutableMap<String, HitItem> = HashMap()

    fun initContent(ctx: Context) {
        val dm = DataManager.getDefault(ctx)
        val hits = dm.getHits()
        dm.closeDb()

        ITEMS.clear()
        ITEM_MAP.clear()

        for ((number, hit) in hits.reversed().withIndex()) {
            addItem(createHitItem(hit, number + 1))
        }
    }

    private fun addItem(item: HitItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    private fun convertDate(dateInMilliseconds: Long): String {
        return DateFormat.format(dateInMilliseconds)
    }

    private fun createHitItem(hit: Hit, number: Int): HitItem {
        return HitItem("$number (${hit.mTimestamp})", convertDate(hit.mTimestamp), hit.mFrameContent, hit)
    }

    private fun makeDetails(context: Context, position: Int): String {
        val builder = StringBuilder()
        builder.append(context.getString(R.string.hit_make_details)).append(position)
        for (i in 0 until position) {
            builder.append("\n").append(context.getString(R.string.hit_make_details_foot))
        }
        return builder.toString()
    }

    class HitItem(val id: String, val content: String, val frame: String, val hit: Hit) {

        override fun toString(): String {
            return content
        }
    }
}
