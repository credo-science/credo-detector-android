package science.credo.credomobiledetektor.database

import android.content.Context
import android.util.Log
import ninja.sakib.pultusorm.annotations.AutoIncrement
import ninja.sakib.pultusorm.annotations.PrimaryKey
import ninja.sakib.pultusorm.core.PultusORM
import ninja.sakib.pultusorm.core.PultusORMCondition
import science.credo.credomobiledetektor.detection.CachedHit
import science.credo.credomobiledetektor.detection.Hit
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.network.NetworkCommunication
import science.credo.credomobiledetektor.network.ServerInterface
import science.credo.credomobiledetektor.network.messages.DetectionRequest
import science.credo.credomobiledetektor.network.messages.DetectionResponse

/**
 * Database management class.
 *
 * This class is used to store both recently detected and already server-synchronized hits. It's trimmed after certain period of time.
 *
 * @property context Android context object.
 */
class DataManager private constructor(context: Context) {
    val mContext = context
    var mAppPath: String = context.getFilesDir().getAbsolutePath()

    var mHitsDb: PultusORM? = null
    var mKeyValueDb: PultusORM? = null
    var mCachedMessagesDb: PultusORM? = null

    val mHitsDBFileName = "hits.db"
    val mKeyValueFileName = "keyvalue.db"
    val mCachedMessagesFileName = "cachedMessages.db"

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

    private fun openCachedMessagesDb() {
        mCachedMessagesDb = PultusORM(mCachedMessagesFileName, mAppPath)
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

    private fun closeCachedMessagesDb() {
        mCachedMessagesDb?.close()
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

    fun storeCachedMessage(message: CachedMessage) {
        if (SI) openCachedMessagesDb()
        mCachedMessagesDb!!.save(message)
        if (SI) closeCachedMessagesDb()
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
     * Retrieves detected hits from the database.
     *
     * @return MutableList<Hit> list containing found Hit objects.
     */
    fun getHits(): MutableList<Hit> {
        if (SI) openHitsDb()
        val hits = mHitsDb!!.find(Hit()) as MutableList<Hit>
        if (SI) closeHitsDb()
        return hits
    }

    fun getCachedHits(): MutableList<CachedHit> {
        if (SI) openHitsDb()
        val hits = mHitsDb!!.find(CachedHit()) as MutableList<CachedHit>
        if (SI) closeHitsDb()
        return hits
    }

    fun getCachedMessages(): MutableList<CachedMessage> {
        if (SI) openCachedMessagesDb()
        val msgs = mCachedMessagesDb!!.find(CachedMessage()) as MutableList<CachedMessage>
        if (SI) closeCachedMessagesDb()
        return msgs
    }

    /**
     * Returns count of detected hits.
     */
    fun getHitsNumber(): Long {
        if (SI) openHitsDb()
        val number = mHitsDb!!.count(Hit())
        if (SI) closeDb()
        return number
    }

    fun getCachedHitsNumber(): Long {
        if (SI) openHitsDb()
        val number = mHitsDb!!.count(CachedHit())
        if (SI) closeDb()
        return number
    }

    /**
     * Stores already uploaded hit.
     *
     * @param hit Hit object to be stored.
     */
    fun storeCachedHit(hit: Hit) {
        val cachedHit = CachedHit(hit.frameInfo, hit.locationInfo, hit.factorInfo)
        storeHit(cachedHit)
    }

    /**
     * Trims hits that are older than pre-defined live period.
     */
    fun trimHitsDb() {
        if (SI) openHitsDb()
        val treshhold = System.currentTimeMillis() - TRIMPERIOD_HITS
        val hits = mHitsDb!!.find(Hit()) as MutableList<Hit>
        val cachedHits = mHitsDb!!.find(CachedHit()) as MutableList<CachedHit>

        for (hit in hits) {
            if (hit.mTimestamp < treshhold) {
                mHitsDb!!.delete(hit)
            }
        }

        for (hit in cachedHits) {
            if (hit.mTimestamp < treshhold) {
                mHitsDb!!.delete(hit)
            }
        }

        if (SI) closeHitsDb()
    }

    fun sendHitsToNetwork() {
        val hits = getHits()
        val serverInterface = ServerInterface.getDefault(mContext)
        val deviceInfo = IdentityInfo.getInstance(mContext).getIdentityData()
        val request = DetectionRequest(hits, deviceInfo)
        val response = serverInterface.sendDetections(request)
    }

    fun flushCachedMessages() {
        val msgs = getCachedMessages()
        openCachedMessagesDb()
        for (msg in msgs) {
            val response = NetworkCommunication.post(msg.mEndpoint, msg.mMessage, msg.mToken)
            when (response.code) {
                in 200..299 -> mCachedMessagesDb!!.delete(msg)
            }
        }
        closeCachedMessagesDb()
    }
}