package science.credo.credomobiledetektor.database

import android.content.Context
import android.util.Log
import ninja.sakib.pultusorm.annotations.AutoIncrement
import ninja.sakib.pultusorm.annotations.PrimaryKey
import ninja.sakib.pultusorm.core.PultusORM
import ninja.sakib.pultusorm.core.PultusORMCondition
import science.credo.credomobiledetektor.R
import science.credo.credomobiledetektor.detection.Hit

/**
 * Created by poznan on 28/08/2017.
 */

class DataManager private constructor (context: Context){
    val mContext = context

    var mHitsDb: PultusORM? = null
    var mKeyValueDb: PultusORM? = null
    var mAppPath: String = context.getFilesDir().getAbsolutePath()

    val mHitsDBFileName = "hits.db"
    val mKeyValueFileName = "keyvalue.db"

//    @deprecated tables
//    var mHitDb: PultusORM? = null
//    var mCachedHitDb: PultusORM? = null
//    val mCachedHitDBFileName = "cached.hit.db"
//    val mHitDBFileName = "hit.db"




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
//            openHitDb()
//            openCachedHitDb()
            openHitsDb()
            openKeyValueDb()
        }
        checkAndUpdateDbSchema()
    }

    fun closeDb() {
        if (!SI) {
//            closeHitDb()
//            closeCachedHitDb()
            closeHitsDb()
            closeKeyValueDb()
        }
    }

//    private fun openHitDb() {mHitDb = PultusORM(mHitDBFileName, mAppPath)}
//    private fun openCachedHitDb() {mCachedHitDb = PultusORM(mCachedHitDBFileName, mAppPath)}
    private fun openHitsDb() {mHitsDb = PultusORM(mHitsDBFileName, mAppPath)}
    private fun openKeyValueDb() {mKeyValueDb = PultusORM(mKeyValueFileName, mAppPath)}
//    private fun closeHitDb() {mHitDb?.close()}
//    private fun closeCachedHitDb() {mCachedHitDb?.close()}
    private fun closeHitsDb() {mHitsDb?.close()}
    private fun closeKeyValueDb() {mKeyValueDb?.close()}

    fun checkAndUpdateDbSchema(): DataManager {
        val schema_key = "database_schema_version"
        if (SI) openKeyValueDb()
        val storedDbSchema: String? = get(schema_key)
        Log.d(TAG, "DBSchema: $storedDbSchema, resources schema: 0.1")
        if (storedDbSchema != "0.1") {
            Log.d(TAG, "resetting schema")
            if (SI) {
                openHitsDb()
            }
            mHitsDb!!.drop(Hit())
            if (SI) {
                closeHitsDb()
            }
            put(schema_key, "0.1")
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
        if (SI) openHitsDb()
        mHitsDb!!.save(hit)
        if (SI) closeDb()
    }

    //storeHitDB(dataString, timestamp, width, height)
    //fun storeHit

    fun removeHit(hit: Hit) {
        if (SI) openHitsDb()
        mHitsDb!!.delete(hit)
        if (SI) closeDb()
    }

    fun getHits() : MutableList<Hit> {
        if (SI) openHitsDb()
        val hits = mHitsDb!!.find(Hit(), isUploaded(false)) as MutableList<Hit>
        if (SI) closeDb()
        return hits
    }

    // @TODO fix
    fun getHitsNumber() : Long {
        return 0
//        if (SI) openHitsDb()
//        val number = mHitsDb!!.count(Hit())
//        if (SI) closeDb()
//        return number
    }

    fun storeCachedHit(hit: Hit){
        hit.mIsUploaded = true
        storeHit(hit)
    }

    fun removeCachedHit(hit: Hit) {
        removeHit(hit)
    }

    fun getCachedHits() : MutableList<Hit> {
        if (SI) openHitsDb()
        val condition : PultusORMCondition = PultusORMCondition.Builder().eq("is_uploaded", true).build()
        val hits = mHitsDb!!.find(Hit(), isUploaded(true)) as MutableList<Hit>
        if (SI) closeDb()
        return hits
    }

    // @TODO fix
    fun getCachedHitsNumber() : Long {
        return 0
//        if (SI) openCachedHitDb()
//        val number = mCachedHitDb!!.count(Hit())
//        if (SI) closeCachedHitDb()
//        return number
    }

    fun trimHitsDb() {
        if (SI) openHitsDb()
        val treshhold = System.currentTimeMillis() - TRIMPERIOD_HITS
        val hits = mHitsDb!!.find(Hit()) as MutableList<Hit>
        for (hit in hits) {
            if (hit.mTimestamp < treshhold) {
                mHitsDb!!.delete(hit)
            }
        }
        if (SI) closeHitsDb()
    }

    fun isUploaded(state: Boolean): PultusORMCondition {
        return PultusORMCondition.Builder().eq("is_uploaded", state).build()
    }
}