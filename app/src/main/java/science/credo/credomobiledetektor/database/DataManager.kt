package science.credo.credomobiledetektor.database

import android.content.Context
import android.content.ServiceConnection
import android.util.Log
import ninja.sakib.pultusorm.annotations.AutoIncrement
import ninja.sakib.pultusorm.annotations.PrimaryKey
import ninja.sakib.pultusorm.core.PultusORM
import ninja.sakib.pultusorm.core.PultusORMCondition
import science.credo.credomobiledetektor.R
import science.credo.credomobiledetektor.detection.Hit
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.network.ServerInterface
import science.credo.credomobiledetektor.network.messages.DetectionRequest

/**
 * Created by poznan on 28/08/2017.
 */

class DataManager private constructor (context: Context){
    val mContext = context
    var mHitDb: PultusORM? = null
    var mCachedHitDb: PultusORM? = null
    var mKeyValueDb: PultusORM? = null
    var mAppPath: String = context.getFilesDir().getAbsolutePath()

    val mCachedHitDBFileName = "cached.hit.db"
    val mHitDBFileName = "hit.db"
    val mKeyValueFileName = "keyvalue.db"
    val mDbSchema = "0.4"



    companion object {
        val TAG = "DataManager"
        val TRIMPERIOD_HITS_DAYS = 10
        val TRIMPERIOD_HITS = 1000 * 3600 * 24 * TRIMPERIOD_HITS_DAYS
        val SI = true
        private var mDataManager: DataManager? = null
        fun getInstance(context: Context): DataManager {
            if (mDataManager == null) {
                mDataManager = DataManager(context)
            }
            return mDataManager!!
        }
    }

    init{
        if (!SI) {
            openHitDb()
            openCachedHitDb()
            openKeyValueDb()
        }
        checkAndUpdateDbSchema()
    }

    fun closeDb() {
        if (!SI) {
            closeHitDb()
            closeCachedHitDb()
            closeKeyValueDb()
        }
    }

    private fun openHitDb() {mHitDb = PultusORM(mHitDBFileName, mAppPath)}
    private fun openCachedHitDb() {mCachedHitDb = PultusORM(mCachedHitDBFileName, mAppPath)}
    private fun openKeyValueDb() {mKeyValueDb = PultusORM(mKeyValueFileName, mAppPath)}
    private fun closeHitDb() {mHitDb?.close()}
    private fun closeCachedHitDb() {mCachedHitDb?.close()}
    private fun closeKeyValueDb() {mKeyValueDb?.close()}

    fun checkAndUpdateDbSchema(): DataManager {
        val schema_key = "database_schema_version"
        if (SI) openKeyValueDb()
        val storedDbSchema: String? = get(schema_key)
        Log.d(TAG, "DBSchema: $storedDbSchema, resources schema: $mDbSchema")
        if (storedDbSchema != mDbSchema) {
            Log.d(TAG, "resetting schema")
            if (SI) {
                openHitDb()
                openCachedHitDb()
            }
            mHitDb!!.drop(Hit())
            mCachedHitDb!!.drop(Hit())
            if (SI) {
                closeHitDb()
                closeCachedHitDb()
            }
            put(schema_key, mDbSchema)
        }
        if (SI) closeKeyValueDb()
        return this
    }

    // Key Value DB


    class KeyValue () {
        @PrimaryKey
        @AutoIncrement
        var id: Int = 0
        var key: String? = null
        var value: String? = null
        constructor(k: String, v: String) : this() { key = k; value = v}
    }

    fun get (key: String): String? {
        if (SI) openKeyValueDb()
        val condition: PultusORMCondition = PultusORMCondition.Builder()
                .eq("key",key)
                .build()
        val values = mKeyValueDb!!.find(KeyValue(),condition)
        if (SI) closeKeyValueDb()
        for (it in values) {
            val keyValue = it as KeyValue
            return keyValue.value
        }
        return null
    }

    fun put (key: String, value: String) {
        if (SI) openKeyValueDb()
        val condition: PultusORMCondition = PultusORMCondition.Builder()
                .eq("key",key)
                .build()
        mKeyValueDb!!.delete(KeyValue(), condition)
        mKeyValueDb!!.save(KeyValue(key, value))
        if (SI) closeKeyValueDb()
    }

    // HitDb
    fun storeHit(hit: Hit) {
        if (SI) openHitDb()
        mHitDb!!.save(hit)
        if (SI) closeDb()
    }

    //storeHitDB(dataString, timestamp, width, height)
    //fun storeHit

    fun removeHit(hit: Hit) {
        if (SI) openHitDb()
        mHitDb!!.delete(hit)
        if (SI) closeDb()
    }

    fun getHits() : MutableList<Hit> {
        if (SI) openHitDb()
        val hits = mHitDb!!.find(Hit()) as MutableList<Hit>
        if (SI) closeDb()
        return hits
    }

    fun getHitsNumber() : Long {
        if (SI) openHitDb()
        val number = mHitDb!!.count(Hit())
        if (SI) closeDb()
        return number
    }

    // Cached HitDb
    fun storeCachedHit(hit: Hit){
        if (SI) openCachedHitDb()
        mCachedHitDb!!.save(hit)
        if (SI) closeCachedHitDb()
    }

    fun removeCachedHit(hit: Hit) {
        if (SI) openCachedHitDb()
        mCachedHitDb!!.delete(hit)
        if (SI) closeCachedHitDb()
    }

    fun getCachedHits() : MutableList<Hit> {
        if (SI) openCachedHitDb()
        val hits = mCachedHitDb!!.find(Hit()) as MutableList<Hit>
        if (SI) closeCachedHitDb()
        return hits
    }

    fun getCachedHitsNumber() : Long {
        if (SI) openCachedHitDb()
        val number = mCachedHitDb!!.count(Hit())
        if (SI) closeCachedHitDb()
        return number
    }

    fun trimCachedHitDb() {
        if (SI) openCachedHitDb()
        val treshhold = System.currentTimeMillis() - TRIMPERIOD_HITS
        val hits = mCachedHitDb!!.find(Hit()) as MutableList<Hit>
        for (hit in hits) {
            if (hit.mTimestamp < treshhold) {
                mCachedHitDb!!.delete(hit)
            }
        }
        if (SI) closeCachedHitDb()
    }
    fun trimHitDb() {
        if (SI) openHitDb()
        val treshhold = System.currentTimeMillis() - TRIMPERIOD_HITS
        val hits = mHitDb!!.find(Hit()) as MutableList<Hit>
        for (hit in hits) {
            if (hit.mTimestamp < treshhold) {
                mHitDb!!.delete(hit)
            }
        }
        if (SI) closeHitDb()
    }

    fun sendHitsToNetwork() {
        val hits = getHits()
        val serverInterface = ServerInterface.getDefault(mContext)
        val deviceInfo = IdentityInfo.getInstance(mContext).getIdentityData()
        val request = DetectionRequest(hits, deviceInfo)
        serverInterface.sendDetections(request)
    }
}