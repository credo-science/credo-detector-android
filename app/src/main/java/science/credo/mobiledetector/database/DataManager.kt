package science.credo.mobiledetector.database

import android.content.Context
import android.util.Log
import com.fasterxml.jackson.annotation.JsonIgnore
import ninja.sakib.pultusorm.annotations.AutoIncrement
import ninja.sakib.pultusorm.annotations.Ignore
import ninja.sakib.pultusorm.annotations.PrimaryKey
import ninja.sakib.pultusorm.core.PultusORM
import ninja.sakib.pultusorm.core.PultusORMCondition
import ninja.sakib.pultusorm.core.PultusORMUpdater
import org.jetbrains.anko.doAsync
import science.credo.mobiledetector.detection.Hit
import science.credo.mobiledetector.events.StatsEvent
import science.credo.mobiledetector.info.ConfigurationInfo
import science.credo.mobiledetector.info.IdentityInfo
import science.credo.mobiledetector.network.ServerInterface
import science.credo.mobiledetector.network.exceptions.ServerException
import science.credo.mobiledetector.network.messages.BaseDeviceInfoRequest
import science.credo.mobiledetector.network.messages.DetectionRequest
import science.credo.mobiledetector.network.messages.PingRequest
import java.util.*

/**
 * Database management class.
 *
 * This class is used to store both recently detected and already server-synchronized hits. It's trimmed after certain period of time.
 *
 * @property context Android context object.
 */
class DataManager private constructor(val context: Context) {
    private val mAppPath: String = context.getFilesDir().getAbsolutePath()

    private val mDbFileName = "cache.db"
    private val mDbSchema = "2"
    private val mDb = PultusORM(mDbFileName, mAppPath)

    companion object {
        const val TAG = "DataManager"
        const val TRIM_PERIOD_HITS_DAYS = 10
        const val TRIM_PERIOD_HITS = 1000 * 3600 * 24 * TRIM_PERIOD_HITS_DAYS

        fun getDefault(context: Context): DataManager {
            return DataManager(context)
        }
    }

    init {
        checkAndUpdateDbSchema()
    }

    fun closeDb() {
        //mDb.close()
    }

    /**
     * Checks schema version, if version differs it also updates hits database.
     *
     * @return DataManager object (this).
     */
    private fun checkAndUpdateDbSchema() {
        if (ConfigurationWrapper(context).dbSchema != mDbSchema) {
            mDb.drop(Hit())
            mDb.drop(PingRequest())
            ConfigurationWrapper(context).dbSchema = mDbSchema
        }
    }

    /**
     * Stores hit in Hits database.
     *
     * @param hit Hit object which will be saved.
     */
    fun storeHit(hit: Hit) {
        mDb.save(hit)
    }

    private fun updateHit(hit: Hit) {
        val condition = PultusORMCondition.Builder().eq("id", hit.id).build()

        val toSent = if(hit.toSent) 1 else 0

        val updater = PultusORMUpdater
                .Builder()
                .set("toSent", toSent)
                .set("serverId", hit.serverId)
                .condition(condition)
                .build()
        mDb.update(hit, updater)
    }

    fun storePing(message: PingRequest) {
        mDb.save(message)
    }

    fun deletePing(ping: PingRequest) {
        val condition = PultusORMCondition.Builder().eq("id", ping.id).build()
        mDb.delete(ping, condition)
    }
    /**
     * Retrieves detected hits from the database.
     *
     * @return MutableList<Hit> list containing found Hit objects.
     */
    fun getHits(): MutableList<Hit> {
        return try {
            mDb.find(Hit()) as MutableList<Hit>
        } catch (e: NullPointerException) {
            LinkedList()
        }
    }

    private fun getCachedPings(): MutableList<PingRequest> {
        return try {
            mDb.find(PingRequest()) as MutableList<PingRequest>
        } catch (e: NullPointerException) {
            LinkedList()
        }
    }

    /**
     * Returns count of detected hits.
     */
    fun getHitsCount(): Long {
        return mDb.count(Hit())
    }

    fun getPingsCount(): Long {
        return mDb.count(PingRequest())
    }

    /**
     * Trims hits that are older than pre-defined live period.
     */
    fun trimHitsDb() {
        val threshold = ((System.currentTimeMillis() - TRIM_PERIOD_HITS) / 10000L).toInt() // PultusORM less condition walkaround

        val condition: PultusORMCondition = PultusORMCondition.Builder()
                .eq("toSent", 0)
                .and()
                .less("detectionTimestamp", threshold)
                .build()

        mDb.delete(Hit(), condition)
    }

    fun sendHitsToNetwork(si: ServerInterface) {

        if (!ConfigurationInfo(context).canUpload) { // TODO: create PrivilegesWrapper, move it
            return
        }

        val condition: PultusORMCondition = PultusORMCondition.Builder()
                .eq("toSent", 1)
                .build()

        val applicationContext = context.applicationContext

        doAsync {
            synchronized(applicationContext) {
                val hits = mDb.find(Hit(), condition) as MutableList<Hit>
                Log.i(TAG, "${Thread.currentThread().id} Try to flush ${hits.size} cached hits")

                if (hits.size > 0) {
                    val deviceInfo = IdentityInfo.getDefault(context).getIdentityData()
                    try {
                        for (hit in hits) {
                            val hitsToSend = LinkedList<Hit>()
                            hitsToSend.add(hit)

                            val request = DetectionRequest.build(deviceInfo, hitsToSend)
                            val response = si.sendDetections(request)
                            Log.i(TAG, "${Thread.currentThread().id} Try to flush ${hit.id} sent")
                            hit.serverId = response.detections[0].id
                            hit.toSent = false
                            updateHit(hit)
                        }
                    } catch (e: ServerException) {
                        if ((e.code != 401).and(e.code in 400..499)) {
                            for (hit in hits) {
                                hit.toSent = false
                                updateHit(hit)
                            }
                        }
                    }
                }

                Log.i(TAG, "${Thread.currentThread().id} Flushing hits finish")
            }
        }
    }

    fun flushCachedPings(si: ServerInterface) {
        if (!ConfigurationInfo(context).canUpload) { // TODO: create PrivilegesWrapper, move it
            return
        }

        val pings = getCachedPings()

        Log.i(TAG, "Try to flush ${pings.size} cached pings")
        for (ping in pings) {
            try {
                si.pingRaw(ping)
                deletePing(ping)
            } catch (e: ServerException) {
                if ((e.code != 401).and(e.code in 400..499)) {
                    deletePing(ping)
                }
            }
        }
    }
}
