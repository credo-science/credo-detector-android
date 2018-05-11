package science.credo.credomobiledetektor.database

import android.content.Context
import android.content.ServiceConnection
import android.util.Log
import ninja.sakib.pultusorm.annotations.AutoIncrement
import ninja.sakib.pultusorm.annotations.PrimaryKey
import ninja.sakib.pultusorm.core.PultusORM
import ninja.sakib.pultusorm.core.PultusORMCondition
import science.credo.credomobiledetektor.detection.Hit
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.network.ServerInterface
import science.credo.credomobiledetektor.network.messages.DetectionRequest

/**
 * Database management class.
 *
 * This class is used to store both recently detected and already server-synchronized hits. It's trimmed after certain period of time.
 *
 * @property context Android context object.
 */
class DataManager private constructor(context: Context) {
    val mContext = context

    var mHitsDb: PultusORM? = null
    var mKeyValueDb: PultusORM? = null
    var mAppPath: String = context.getFilesDir().getAbsolutePath()

    val mHitsDBFileName = "hits.db"
    val mKeyValueFileName = "keyvalue.db"

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

    init {
        if (!SI) {
            openHitsDb()
            openKeyValueDb()
        }
        checkAndUpdateDbSchema()
    }

    /**
     * Opens hits database.
     */
    private fun openHitsDb() {
        mHitsDb = PultusORM(mHitsDBFileName, mAppPath)
    }

    /**
     * Opens Key-Value database.
     */
    private fun openKeyValueDb() {
        mKeyValueDb = PultusORM(mKeyValueFileName, mAppPath)
    }

    /**
     * Closes hits database.
     */
    private fun closeHitsDb() {
        mHitsDb?.close()
    }

    /**
     * Closes Key-Value databse.
     */
    private fun closeKeyValueDb() {
        mKeyValueDb?.close()
    }

    /**
     * Closes both databases.
     */
    fun closeDb() {
        if (!SI) {
            closeHitsDb()
            closeKeyValueDb()
        }
    }

    /**
     * Checks schema version, if version differs it also updates hits database.
     *
     * @return DataManager object (this).
     */
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

    /**
     *  Model for KeyValue database.
     */
    class KeyValue() {
        @PrimaryKey
        @AutoIncrement
        var id: Int = 0
        var key: String? = null
        var value: String? = null

        constructor(k: String, v: String) : this() {
            key = k; value = v
        }
    }

    /**
     * Retrieve value from KeyValue database based on passed key.
     *
     * @param key an unique key that is used in search query.
     */
    fun get(key: String): String? {
        if (SI) openKeyValueDb()
        val condition: PultusORMCondition = PultusORMCondition.Builder()
            .eq("key", key)
            .build()
        val values = mKeyValueDb!!.find(KeyValue(), condition)
        if (SI) closeKeyValueDb()
        for (it in values) {
            val keyValue = it as KeyValue
            return keyValue.value
        }
        return null
    }

    /**
     * Stores value in KeyValue database.
     *
     * @param key an unique key.
     * @param value data to store.
     */
    fun put(key: String, value: String) {
        if (SI) openKeyValueDb()
        val condition: PultusORMCondition = PultusORMCondition.Builder()
            .eq("key", key)
            .build()
        mKeyValueDb!!.delete(KeyValue(), condition)
        mKeyValueDb!!.save(KeyValue(key, value))
        if (SI) closeKeyValueDb()
    }

    /**
     * Stores hit in Hits database.
     *
     * @param hit Hit object which will be saved.
     */
    fun storeHit(hit: Hit) {
        if (SI) openHitsDb()
        mHitsDb!!.save(hit)
        if (SI) closeDb()
    }

    /**
     * Removes hit from Hits database.
     *
     * @param hit Hit object which will be deleted.
     */
    fun removeHit(hit: Hit) {
        if (SI) openHitsDb()
        mHitsDb!!.delete(hit)
        if (SI) closeDb()
    }

    /**
     * Retrieves detected or cached hits from the database.
     *
     * @param uploaded determines what to returns - if true - returns cached (already synchronized) results, if false - returns detections to be synchronized.
     * @return MutableList<Hit> list containing found Hit objects.
     */
    fun getHits(uploaded: Boolean): MutableList<Hit> {
        if (SI) openHitsDb()
        val hits = mHitsDb!!.find(Hit(), isUploaded(uploaded)) as MutableList<Hit>
        if (SI) closeHitsDb()
        return hits
    }

    /**
     * Returns count of detected hits.
     */
    // @TODO fix
    fun getHitsNumber(): Long {
        return 0
//        if (SI) openHitsDb()
//        val number = mHitsDb!!.count(Hit())
//        if (SI) closeDb()
//        return number
    }

    /**
     * Stores already uploaded hit.
     *
     * @param hit Hit object to be stored.
     */
    fun storeCachedHit(hit: Hit) {
        hit.mIsUploaded = true
        storeHit(hit)
    }

    // @TODO fix
    fun getCachedHitsNumber(): Long {
        return 0
//        if (SI) openCachedHitDb()
//        val number = mCachedHitDb!!.count(Hit())
//        if (SI) closeCachedHitDb()
//        return number
    }

    /**
     * Trims hits that are older than pre-defined live period.
     */
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

    /**
     * Helper function.
     *
     * @param state which upload state to look for.
     * @return PultusORMCondition object used to narrow results based on is_uploaded column (determines if hit needs to be synchronized or if is already cached).
     */
    fun isUploaded(state: Boolean): PultusORMCondition {
        return PultusORMCondition.Builder().eq("is_uploaded", state).build()
    }

    fun sendHitsToNetwork() {
        val hits = getHits(false)
        val serverInterface = ServerInterface.getDefault(mContext)
        val deviceInfo = IdentityInfo.getInstance(mContext).getIdentityData()
        val request = DetectionRequest(hits, deviceInfo)
        serverInterface.sendDetections(request)
    }
}