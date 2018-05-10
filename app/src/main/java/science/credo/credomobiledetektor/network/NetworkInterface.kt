package science.credo.credomobiledetektor.network

import android.content.Context
import android.util.Log
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import science.credo.credomobiledetektor.database.DataManager
import science.credo.credomobiledetektor.info.ConfigurationInfo
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.UserInfo
import science.credo.credomobiledetektor.network.message.`in`.ErrorFrame
import science.credo.credomobiledetektor.network.message.out.*

/**
 * This class is a bridge between application and external API service.
 *
 * @property mContext Android context object.
 * @property mMapper JSON formatter.
 * @property mConfigurationManager ConfigurationInfo object - contains application config.
 * @property mDataManager DataManager object - is used to communicate with database.
 * @property mIdentityInfo IdentityInfo object - contains all informations which are required by external API.
 */
class NetworkInterface (context: Context){

    val mContext = context
    val mMapper = jacksonObjectMapper()
    val mConfigurationManager = ConfigurationInfo(context)
    val mDataManager = DataManager.getInstance(context)
    val mIdentityInfo = IdentityInfo.getInstance(context)

    /**
     * Sends request.
     *
     * @param endpoint Endpoint to which request is made.
     * @param outFrame OutFrame object - contains request data.
     */
    fun send(endpoint: String, outFrame: OutFrame): NetworkCommunication.Response {
        return NetworkCommunication.post(endpoint, mMapper.writeValueAsString(outFrame))
    }

    /**
     * Sends Ping request.
     *
     * @return boolean based on success.
     */
    fun sendPing() : Boolean {
        val deviceData = mIdentityInfo.getIdentityData()
        //@TODO finish
        //@TODO pass StatsEvent instance
//        val result = send("/ping", PingFrame(deviceData, stats))
        return false
    }

    /**
     * Sends UserInfo request.
     *
     * @return boolean based on success.
     */
    fun sendUserInfo(): Boolean {
        val userData = UserInfo.getNewInstance(mContext).getUserDataInfo()
        val deviceData = mIdentityInfo.getIdentityData()
        val result = send("/user/info", UserInfoFrame(userData, deviceData))
        return result.code == ok
    }

    /**
     * Sends Register request.
     *
     * @return boolean based on success.
     */
    fun sendRegister(): NetworkCommunication.Response {
        val userData = UserInfo.getNewInstance(mContext).getUserDataRegister()
        val deviceData = mIdentityInfo.getIdentityData()
        val result = send("/user/register", RegisterFrame(userData, deviceData))
        return result
    }

    /**
     * Sends Login request.
     *
     * @return boolean based on success.
     */
    fun sendLogin(): NetworkCommunication.Response {
        val userData = UserInfo.getNewInstance(mContext).getUserDataLogin()
        val deviceData = mIdentityInfo.getIdentityData()
        val result = send("/user/login", LoginFrame(userData, deviceData))
        return result
    }

    /**
     * Helper function.
     *
     * @return ErrorFrame object
     */
    fun getError(message: String) : ErrorFrame? {
        return mMapper.readValue(message, ErrorFrame::class.java)
    }

    /**
     * Synchronizes local detections with the server.
     *
     * @return boolean based on success.
     */
    fun sendHitsToNetwork(): Boolean {
        if (mConfigurationManager.canUpload) {
            val hits = mDataManager.getHits(false)
            val deviceData = mIdentityInfo.getIdentityData()
            val result = send("/detection", DetectionsFrame(hits, deviceData))
            Log.d("upload.message",result.message)
            Log.d("upload.code",result.code.toString())
            if (result.code != ok) return false
            for (hit in hits) {
                mDataManager.storeCachedHit(hit)
                mDataManager.removeHit(hit)
            }
            return true
        } else {
            return false
        }
    }

    companion object {
        private var mNetworkInterface: NetworkInterface? = null
        val ok = 200
        val notImplemented = 501
        val error = 400
        val forbidden = 403
        fun getInstance(context: Context): NetworkInterface {
            if (mNetworkInterface == null) {
                mNetworkInterface = NetworkInterface(context)
            }
            return mNetworkInterface!!
        }
    }
}